/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.engine.impl.persistence.entity.data.impl;

import java.util.List;

import org.flowable.cmmn.engine.CmmnEngineConfiguration;
import org.flowable.cmmn.engine.impl.persistence.entity.SentryOnPartInstanceEntity;
import org.flowable.cmmn.engine.impl.persistence.entity.SentryOnPartInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.data.AbstractCmmnDataManager;
import org.flowable.cmmn.engine.impl.persistence.entity.data.SentryOnPartInstanceDataManager;

/**
 * @author Joram Barrez
 */
public class MybatisSentryOnPartInstanceDataManagerImpl extends AbstractCmmnDataManager<SentryOnPartInstanceEntity> implements SentryOnPartInstanceDataManager {

    public MybatisSentryOnPartInstanceDataManagerImpl(CmmnEngineConfiguration cmmnEngineConfiguration) {
        super(cmmnEngineConfiguration);
    }

    @Override
    public Class<? extends SentryOnPartInstanceEntity> getManagedEntityClass() {
        return SentryOnPartInstanceEntityImpl.class;
    }

    @Override
    public SentryOnPartInstanceEntity create() {
        return new SentryOnPartInstanceEntityImpl();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SentryOnPartInstanceEntity> findSentryOnPartInstancesByPlanItemInstanceId(String planItemInstanceId) {
        return getDbSqlSession().selectList("selectSentryOnPartInstanceByPlanItemInstanceId", planItemInstanceId);
    }

}
