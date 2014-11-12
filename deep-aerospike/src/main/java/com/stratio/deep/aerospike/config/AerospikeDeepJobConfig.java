/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.deep.aerospike.config;

import com.aerospike.client.query.Statement;
import com.aerospike.hadoop.mapreduce.AerospikeConfigUtil;
import com.stratio.deep.aerospike.extractor.AerospikeCellExtractor;
import com.stratio.deep.aerospike.extractor.AerospikeEntityExtractor;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.config.HadoopConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.filter.Filter;
import com.stratio.deep.commons.filter.FilterOperator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import scala.Tuple2;
import scala.Tuple3;

import java.io.Serializable;
import java.util.*;

import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.*;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.SPLIT_SIZE;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.USE_CHUNKS;

public class AerospikeDeepJobConfig<T> extends HadoopConfig<T> implements IAerospikeDeepJobConfig<T>, Serializable{

    private static final long serialVersionUID = 2778930913494063818L;

    /**
     * configuration to be broadcasted to every spark node
     */
    private transient Configuration configHadoop;

    /**
     * A list of aerospike hosts to connect
     */
    private List<String> hostList = new ArrayList<>();

    /**
     * A list of aerospike ports to connect
     */
    private List<Integer> portList = new ArrayList<>();

    /**
     * Aerospike's set name
     */
    private String set;

    /**
     * Aerospike's bin name
     */
    private String bin;

    /**
     * Aerospike's operation
     */
    private String operation = AerospikeConfigUtil.DEFAULT_INPUT_OPERATION; //scan

    /**
     * Aerospike's equality filter value
     */
    private Tuple2<String, Object> equalsFilter;

    /**
     * Aerospike's numrange filter value
     */
    private Tuple3<String, Object, Object> numrangeFilter;

    public AerospikeDeepJobConfig(Class<T> entityClass) {
        super(entityClass);
        if(Cells.class.isAssignableFrom(entityClass)){
            extractorImplClass = AerospikeCellExtractor.class;
        }else{
            extractorImplClass = AerospikeEntityExtractor.class;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        return !hostList.isEmpty() ? hostList.get(0) : null;
    }

    public List<String> getHostList() {
        return hostList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> host(String host) {
        this.hostList.add(host);
        return this;
    }


    public AerospikeDeepJobConfig<T> port(int port) {
        this.portList.add(port);
        return this;
    }

    public Integer getAerospikePort() {
        return !portList.isEmpty() ? portList.get(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> host(List<String> host) {
        this.hostList.addAll(host);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> port(List<Integer> port) {
        this.portList.addAll(port);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> namespace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public AerospikeDeepJobConfig<T> host(String[] hosts) {
        this.hostList.addAll(Arrays.asList(hosts));
        return this;
    }

    public AerospikeDeepJobConfig<T> port(Integer[] ports) {
        this.portList.addAll(Arrays.asList(ports));
        return this;
    }

    @Override
    public AerospikeDeepJobConfig<T> set(String set) {
        this.set = set;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.nameSpace;
    }

    @Override
    public String getSet() {
        return this.set;
    }

    @Override
    public AerospikeDeepJobConfig<T> bin(String bin) {
        this.bin = bin;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getInputColumns() {
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> inputColumns(String... columns) {
        fields = columns;
        return this;
    }

    @Override
    public String getBin() {
        return this.bin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> operation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOperation() {
        return this.operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T>  equalsFilter(Tuple2<String, Object> filter) {
        this.equalsFilter = filter;
        return this;
    }

    public Tuple2<String, Object> getEqualsFilter() {
        return this.equalsFilter;
    }

    @Override
    public AerospikeDeepJobConfig<T> numrangeFilter(Tuple3<String, Object, Object> filter) {
        this.numrangeFilter = filter;
        return this;
    }

    public Tuple3<String, Object, Object> getNumrangeFilter() {
        return this.numrangeFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> initialize() {
        validate();

        configHadoop = new JobConf();
        configHadoop = new Configuration();

        configHadoop.set(AerospikeConfigUtil.INPUT_HOST, getHost());

        configHadoop.set(AerospikeConfigUtil.INPUT_PORT, Integer.toString(getAerospikePort()));

        configHadoop.set(AerospikeConfigUtil.INPUT_NAMESPACE, nameSpace);

        configHadoop.set(AerospikeConfigUtil.INPUT_SETNAME, set);

        configHadoop.set(AerospikeConfigUtil.INPUT_OPERATION, operation);

        if(numrangeFilter != null) {
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_BIN, numrangeFilter._1());
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_BEGIN, numrangeFilter._2().toString());
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_END, numrangeFilter._3().toString());
        }

        configHadoop.set(AerospikeConfigUtil.OUTPUT_HOST, getHost());

        configHadoop.set(AerospikeConfigUtil.OUTPUT_PORT, Integer.toString(getAerospikePort()));

        configHadoop.set(AerospikeConfigUtil.OUTPUT_NAMESPACE, nameSpace);

        configHadoop.set(AerospikeConfigUtil.OUTPUT_SETNAME, set);

        if (operation != null) {
            configHadoop.set(AerospikeConfigUtil.INPUT_OPERATION, operation);
        }

        return this;
    }

    /**
     * validates connection parameters
     */
    private void validate() {
        if (hostList.isEmpty()) {
            throw new IllegalArgumentException("host cannot be null");
        }
        if (nameSpace == null) {
            throw new IllegalArgumentException("namespace cannot be null");
        }
        if (set == null) {
            throw new IllegalArgumentException("set cannot be null");
        }
        if (portList.isEmpty()) {
            throw new IllegalArgumentException("port cannot be null");
        }
        if (hostList.size() != portList.size()) {
            throw new IllegalArgumentException("Host and ports cardinality must be the same");
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getHadoopConfiguration() {
        if (configHadoop == null) {
            initialize();
        }
        return configHadoop;
    }

    @Override
    public AerospikeDeepJobConfig<T> initialize(ExtractorConfig extractorConfig) {
        Map<String, Serializable> values = extractorConfig.getValues();

        if (values.get(HOST) != null) {
            host((extractorConfig.getStringArray(HOST)));
        }

        if(values.get(PORT)!=null){
            port((extractorConfig.getInteger(PORT)));
        }

        if(values.get(NAMESPACE)!=null){
            namespace(extractorConfig.getString(NAMESPACE));
        }

        if (values.get(SET) != null) {
            set(extractorConfig.getString(SET));
        }

        if (values.get(FILTER_QUERY) != null) {
            filterQuery(extractorConfig.getFilterArray(FILTER_QUERY));
        }

        if (values.get(INPUT_COLUMNS) != null) {
            inputColumns(extractorConfig.getStringArray(INPUT_COLUMNS));
        }

        this.initialize();

        return this;
    }

    public AerospikeDeepJobConfig<T> filterQuery (Filter[] filters){
        if(filters.length > 1) {
            throw new UnsupportedOperationException("Aerospike currently accepts only one filter operations");
        }
        else if(filters.length>0) {
            Filter deepFilter = filters[0];
            if(!isValidAerospikeFilter(deepFilter)) {
                throw new UnsupportedOperationException("Aerospike currently supports only equality and range filter operations");
            } else if(!deepFilter.getOperation().equals(FilterOperator.IS)) {
                operation("numrange");
                setAerospikeNumrange(deepFilter);
            } else {
                operation("scan");
                setAerospikeEqualsFilter(deepFilter);
            }
        }
        return this;
    }

    private boolean isValidAerospikeFilter(Filter filter) {
        return filter.getOperation().equals(FilterOperator.IS) ||
                filter.getOperation().equals(FilterOperator.LT) ||
                filter.getOperation().equals(FilterOperator.GT) ||
                filter.getOperation().equals(FilterOperator.GTE) ||
                filter.getOperation().equals(FilterOperator.LTE);
    }

    private void setAerospikeNumrange(Filter filter) {
        String field = filter.getField();
        if(!filter.getValue().getClass().equals(Long.class)){
            throw new UnsupportedOperationException("Range filters only accept Long type as parameters");
        }
        if(filter.getOperation().equals(FilterOperator.LT)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, Long.MIN_VALUE, (Long)filter.getValue() -1));
        }
        if(filter.getOperation().equals(FilterOperator.LTE)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, Long.MIN_VALUE, (Long)filter.getValue()));
        }
        if(filter.getOperation().equals(FilterOperator.GT)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, (Long)filter.getValue() + 1, Long.MAX_VALUE));
        }
        if(filter.getOperation().equals(FilterOperator.GTE)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, (Long)filter.getValue(), Long.MAX_VALUE));
        }
    }

    private void setAerospikeEqualsFilter(Filter filter) {

        equalsFilter(new Tuple2<String, Object>(filter.getField(), filter.getValue()));
    }


}