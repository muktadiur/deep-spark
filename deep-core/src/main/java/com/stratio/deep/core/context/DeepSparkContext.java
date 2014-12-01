/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.stratio.deep.core.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;

import com.stratio.deep.commons.config.BaseConfig;
import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.commons.querybuilder.UpdateQueryBuilder;
import com.stratio.deep.core.function.PrepareSaveFunction;
import com.stratio.deep.core.hdfs.utils.MapSchemaFromLines;
import com.stratio.deep.core.hdfs.utils.SchemaMap;
import com.stratio.deep.core.hdfs.utils.TableName;
import com.stratio.deep.core.hdfs.utils.TextFileDataTable;
import com.stratio.deep.core.rdd.DeepJavaRDD;
import com.stratio.deep.core.rdd.DeepRDD;

/**
 * Entry point to the Cassandra-aware Spark context.
 *
 * @author Luca Rosellini <luca@stratio.com>
 */
public class DeepSparkContext extends JavaSparkContext implements Serializable {

    /**
     * The constant LOG.
     */
    private static final Logger LOG = Logger.getLogger(DeepSparkContext.class);

    /**
     * Overridden superclass constructor.
     *
     * @param sc an already created spark context.
     */
    public DeepSparkContext(SparkContext sc) {
        super(sc);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master  the url of the master node.
     * @param appName the name of the application.
     */
    public DeepSparkContext(String master, String appName) {
        super(master, appName);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master    the url of the master node.
     * @param appName   the name of the application.
     * @param sparkHome the spark home folder.
     * @param jarFile   the jar file to serialize and send to all the cluster nodes.
     */
    public DeepSparkContext(String master, String appName, String sparkHome, String jarFile) {
        super(master, appName, sparkHome, jarFile);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master    the url of the master node.
     * @param appName   the name of the application.
     * @param sparkHome the spark home folder.
     * @param jars      the jar file(s) to serialize and send to all the cluster nodes.
     */
    public DeepSparkContext(String master, String appName, String sparkHome, String[] jars) {
        super(master, appName, sparkHome, jars);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master      the url of the master node.
     * @param appName     the name of the application.
     * @param sparkHome   the spark home folder.
     * @param jars        the jar file(s) to serialize and send to all the cluster nodes.
     * @param environment a map of environment variables.
     */
    public DeepSparkContext(String master, String appName, String sparkHome, String[] jars,
                            Map<String, String> environment) {

        super(master, appName, sparkHome, jars, environment);
    }

    /**
     * Creates a RDD.
     *
     * @param <T>    the type parameter
     * @param config the config
     * @return the rDD
     */
    public <T> RDD<T> createRDD(ExtractorConfig<T> config) {
        return new DeepRDD<>(this.sc(), config);
    }

    public <T> RDD<T> createRDD(DeepJobConfig<T, ?> deepJobConfig) {
        return new DeepRDD<>(this.sc(), deepJobConfig);
    }

    /**
     * Creates a JavaRDD.
     *
     * @param <T>    the type parameter
     * @param <>     the type parameter
     * @param config the config
     * @return the java rDD
     */
    public <T> JavaRDD<T> createJavaRDD(
            ExtractorConfig<T> config) {
        return (JavaRDD<T>) new DeepJavaRDD<>((DeepRDD<T, ExtractorConfig<T>>) createRDD(config));
    }

    public <T> JavaRDD<T> createJavaRDD(DeepJobConfig<T, ?> config) {
        return (JavaRDD<T>) new DeepJavaRDD<>((DeepRDD<T, DeepJobConfig<T, ?>>) createRDD(config));
    }

    /**
     * @param rdd
     * @param config
     * @param queryBuilder
     * @param <T>
     * @param <S>
     */
    public static <T, S extends BaseConfig<T>> void saveRDD(RDD<T> rdd, S config, UpdateQueryBuilder queryBuilder) {

        config.setRddId(rdd.id());
        config.setPartitionId(0);
        rdd.foreachPartition(new PrepareSaveFunction<>(queryBuilder, config, rdd.first()));
    }

    /**
     * @param rdd
     * @param config
     * @param <T>
     * @param <S>
     */
    public static <T, S extends BaseConfig<T>> void saveRDD(RDD<T> rdd, S config) {
        saveRDD(rdd, config, null);

    }

    public JavaRDD<Cells> createHDFSRDD(ExtractorConfig<Cells> config) {

        Serializable host = config.getValues().get(ExtractorConstants.HOST);
        Serializable port = config.getValues().get(ExtractorConstants.PORT);
        Serializable path = config.getValues().get(ExtractorConstants.HDFS_FILE_PATH);

        final TextFileDataTable textFileDataTable = createTextFileMetaDataFromConfig(config);

        if (config.getExtractorImplClassName().equals(ExtractorConstants.HDFS)) {
            path = ExtractorConstants.HDFS_PREFIX + host.toString() + ":" + port + path.toString();
        } else {
            path = path.toString();
        }
        RDD<String> result = this.sc().textFile(path.toString(), 1);

        JavaRDD<Cells> resultCells = result.toJavaRDD().map(new MapSchemaFromLines(textFileDataTable));

        return resultCells;
    }

    private TextFileDataTable createTextFileMetaDataFromConfig(ExtractorConfig<Cells> extractorConfig) {

        if (extractorConfig.getValues().get(ExtractorConstants.HDFS_FILEDATATABLE) != null) {
            final TextFileDataTable textFileDataTable = (TextFileDataTable) extractorConfig.getValues()
                    .get(ExtractorConstants.HDFS_FILEDATATABLE);

            return textFileDataTable;
        } else {

            Serializable separator = extractorConfig.getValues().get(ExtractorConstants.HDFS_FILE_SEPARATOR);
            String catalogName = (String) extractorConfig.getValues().get(ExtractorConstants.CATALOG);
            String tableName = (String) extractorConfig.getValues().get(ExtractorConstants.TABLE);

            final String splitSep = separator.toString();
            final ArrayList<SchemaMap<?>> columns = (ArrayList<SchemaMap<?>>) extractorConfig.getValues().get
                    (ExtractorConstants.HDFS_SCHEMA);

            final TextFileDataTable textFileDataTableTemp = new TextFileDataTable(new TableName(catalogName, tableName),
                    columns);
            textFileDataTableTemp.setLineSeparator(splitSep);
            return textFileDataTableTemp;
        }

    }

}