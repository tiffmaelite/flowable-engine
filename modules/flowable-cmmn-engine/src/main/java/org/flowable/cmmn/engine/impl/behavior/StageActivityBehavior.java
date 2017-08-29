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
package org.flowable.cmmn.engine.impl.behavior;

import org.flowable.cmmn.engine.impl.persistence.entity.PlanItemInstanceEntity;
import org.flowable.cmmn.engine.impl.util.CommandContextUtil;
import org.flowable.cmmn.engine.runtime.DelegatePlanItemInstance;
import org.flowable.cmmn.model.Stage;

/**
 * @author Joram Barrez
 */
public class StageActivityBehavior implements CmmnTriggerableActivityBehavior {
    
    protected Stage stage;
    
    public StageActivityBehavior(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void execute(DelegatePlanItemInstance delegatePlanItemInstance) {
        PlanItemInstanceEntity stagePlanItemInstanceEntity = (PlanItemInstanceEntity) delegatePlanItemInstance;
        stagePlanItemInstanceEntity.setName(delegatePlanItemInstance.getPlanItem().getName());
        CommandContextUtil.getAgenda().planInitStageOperation(stagePlanItemInstanceEntity);
    }
    
    @Override
    public void trigger(DelegatePlanItemInstance planItemInstance) {
        
    }

}
