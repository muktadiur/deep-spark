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
package com.stratio.deep.aerospike;

import static org.testng.Assert.assertEquals;

import org.apache.spark.rdd.RDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.stratio.deep.aerospike.extractor.AerospikeCellExtractor;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.commons.filter.Filter;
import com.stratio.deep.commons.filter.FilterType;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.core.extractor.ExtractorCellTest;
import com.stratio.deep.core.extractor.ExtractorTest;

@Test(groups = { "AerospikeCellExtractorFT", "FunctionalTests" }, dependsOnGroups = { "AerospikeJavaRDDFT" })
public class AerospikeCellExtractorFT extends ExtractorCellTest {

    private static final Logger LOG = LoggerFactory.getLogger(AerospikeCellExtractorFT.class);

    public AerospikeCellExtractorFT() {
        super(AerospikeCellExtractor.class, AerospikeJavaRDDFT.HOST, AerospikeJavaRDDFT.PORT, true);
    }

    @Test
    @Override
    public void testDataSet() {
        DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");

        try {

            ExtractorConfig<Cells> inputConfigEntity = new ExtractorConfig(Cells.class);
            inputConfigEntity.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, ExtractorTest.BOOK_INPUT);
            inputConfigEntity.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity = context.createRDD(inputConfigEntity);

            //Import dataSet was OK and we could read it
            assertEquals(inputRDDEntity.count(), 1, "Expected read entity count is 1");

        } finally {
            context.stop();
        }

    }

    @Test
    @Override
    protected void testFilterEQ() {
        DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");
        try {

            Filter[] filters = null;
            Filter equalFilter = new Filter("number", FilterType.EQ, 3L);
            Filter ltFilter = new Filter("number", FilterType.LT, 4L);
            Filter gtFilter = new Filter("number", FilterType.GT, 5L);
            Filter lteFilter = new Filter("number", FilterType.LTE, 3L);
            Filter gteFilter = new Filter("number", FilterType.GTE, 4L);
            Filter equalFilter2 = new Filter("number", FilterType.EQ, 4L);

            try {
                filters = new Filter[] { equalFilter, ltFilter };
                ExtractorConfig inputConfigEntity = getFilterConfig(filters);
            } catch (UnsupportedOperationException e) {
                LOG.info("Expected exception thrown for more than one filter in aerospike");
            }

            try {
                filters = new Filter[] { new Filter("number", FilterType.NEQ, "invalid") };
                ExtractorConfig inputConfigEntity = getFilterConfig(filters);
            } catch (UnsupportedOperationException e) {
                LOG.info("Expected exception thrown for a filter not supported by aerospike");
            }

            try {
                Filter invalidFilter = new Filter("number", FilterType.LT, "invalid");
                filters = new Filter[] { invalidFilter };
                ExtractorConfig inputConfigEntity = getFilterConfig(filters);
            } catch (UnsupportedOperationException e) {
                LOG.info("Expected exception thrown for using a range filter without Long mandatory value type");
            }

            ExtractorConfig<Cells> inputConfigEntity = new ExtractorConfig(Cells.class);
            inputConfigEntity.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { equalFilter });
            inputConfigEntity.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity = context.createRDD(inputConfigEntity);
            assertEquals(inputRDDEntity.count(), 1, "Expected read entity count is 1");

            ExtractorConfig<Cells> inputConfigEntity2 = new ExtractorConfig(Cells.class);
            inputConfigEntity2.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { ltFilter });
            inputConfigEntity2.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity2 = context.createRDD(inputConfigEntity2);
            assertEquals(inputRDDEntity2.count(), 1, "Expected read entity count is 1");

            ExtractorConfig<Cells> inputConfigEntity3 = new ExtractorConfig(Cells.class);
            inputConfigEntity3.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { gtFilter });
            inputConfigEntity3.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity3 = context.createRDD(inputConfigEntity3);
            assertEquals(inputRDDEntity3.count(), 0, "Expected read entity count is 0");

            ExtractorConfig<Cells> inputConfigEntity4 = new ExtractorConfig(Cells.class);
            inputConfigEntity4.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { lteFilter });
            inputConfigEntity4.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity4 = context.createRDD(inputConfigEntity4);
            assertEquals(inputRDDEntity4.count(), 1, "Expected read entity count is 1");

            ExtractorConfig<Cells> inputConfigEntity5 = new ExtractorConfig(Cells.class);
            inputConfigEntity5.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { gteFilter });
            inputConfigEntity5.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity5 = context.createRDD(inputConfigEntity5);
            assertEquals(inputRDDEntity5.count(), 0, "Expected read entity count is 0");

            ExtractorConfig<Cells> inputConfigEntity6 = new ExtractorConfig(Cells.class);
            inputConfigEntity6.putValue(ExtractorConstants.HOST, AerospikeJavaRDDFT.HOST)
                    .putValue(ExtractorConstants.PORT, AerospikeJavaRDDFT.PORT)
                    .putValue(ExtractorConstants.NAMESPACE, AerospikeJavaRDDFT.NAMESPACE_CELL)
                    .putValue(ExtractorConstants.SET, "input")
                    .putValue(ExtractorConstants.FILTER_QUERY, new Filter[] { gteFilter });
            inputConfigEntity6.setExtractorImplClass(AerospikeCellExtractor.class);

            RDD<Cells> inputRDDEntity6 = context.createRDD(inputConfigEntity5);
            assertEquals(inputRDDEntity6.count(), 0, "Expected read entity count is 0");

        } finally {
            context.stop();
        }
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    @Override
    public void testFilterNEQ() {
        super.testFilterNEQ();
    }
}
