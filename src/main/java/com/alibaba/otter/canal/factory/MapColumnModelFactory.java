package com.alibaba.otter.canal.factory;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * {@link IModelFactory} implementation that builds entity instances from a column-name-to-value
 * map, using MyBatis-Plus table metadata to map columns to entity properties.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class MapColumnModelFactory extends AbstractModelFactory<Map<String, String>> {

    @Override
    <R> R newInstance(Class<R> tableClass, Map<String, String> valueMap) throws Exception {
        R object = BeanUtils.instantiateClass(tableClass);
        // Obtain MyBatis-Plus table annotation metadata
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableClass);
        // Iterate over the table field metadata
        for (TableFieldInfo tableFieldInfo:  tableInfo.getFieldList()) {
            // Set the property value mapped to the matching column
            Object value = MapUtils.getObject(valueMap, tableFieldInfo.getColumn());
            PropertyUtils.setProperty(object, tableFieldInfo.getProperty(), value);
        }
        return object;
    }

}
