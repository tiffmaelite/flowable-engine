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
package org.flowable.engine.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.engine.TaskService;
import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.cmd.AddCommentCmd;
import org.flowable.engine.impl.cmd.AddIdentityLinkCmd;
import org.flowable.engine.impl.cmd.ClaimTaskCmd;
import org.flowable.engine.impl.cmd.CompleteTaskCmd;
import org.flowable.engine.impl.cmd.CompleteTaskWithFormCmd;
import org.flowable.engine.impl.cmd.CreateAttachmentCmd;
import org.flowable.engine.impl.cmd.DelegateTaskCmd;
import org.flowable.engine.impl.cmd.DeleteAttachmentCmd;
import org.flowable.engine.impl.cmd.DeleteCommentCmd;
import org.flowable.engine.impl.cmd.DeleteIdentityLinkCmd;
import org.flowable.engine.impl.cmd.DeleteTaskCmd;
import org.flowable.engine.impl.cmd.GetAttachmentCmd;
import org.flowable.engine.impl.cmd.GetAttachmentContentCmd;
import org.flowable.engine.impl.cmd.GetCommentCmd;
import org.flowable.engine.impl.cmd.GetIdentityLinksForTaskCmd;
import org.flowable.engine.impl.cmd.GetProcessInstanceAttachmentsCmd;
import org.flowable.engine.impl.cmd.GetProcessInstanceCommentsCmd;
import org.flowable.engine.impl.cmd.GetSubTasksCmd;
import org.flowable.engine.impl.cmd.GetTaskAttachmentsCmd;
import org.flowable.engine.impl.cmd.GetTaskCommentsByTypeCmd;
import org.flowable.engine.impl.cmd.GetTaskCommentsCmd;
import org.flowable.engine.impl.cmd.GetTaskDataObjectCmd;
import org.flowable.engine.impl.cmd.GetTaskDataObjectsCmd;
import org.flowable.engine.impl.cmd.GetTaskEventCmd;
import org.flowable.engine.impl.cmd.GetTaskEventsCmd;
import org.flowable.engine.impl.cmd.GetTaskFormModelCmd;
import org.flowable.engine.impl.cmd.GetTaskVariableCmd;
import org.flowable.engine.impl.cmd.GetTaskVariableInstanceCmd;
import org.flowable.engine.impl.cmd.GetTaskVariableInstancesCmd;
import org.flowable.engine.impl.cmd.GetTaskVariablesCmd;
import org.flowable.engine.impl.cmd.GetTasksLocalVariablesCmd;
import org.flowable.engine.impl.cmd.GetTypeCommentsCmd;
import org.flowable.engine.impl.cmd.HasTaskVariableCmd;
import org.flowable.engine.impl.cmd.NewTaskCmd;
import org.flowable.engine.impl.cmd.RemoveTaskVariablesCmd;
import org.flowable.engine.impl.cmd.ResolveTaskCmd;
import org.flowable.engine.impl.cmd.SaveAttachmentCmd;
import org.flowable.engine.impl.cmd.SaveCommentCmd;
import org.flowable.engine.impl.cmd.SaveTaskCmd;
import org.flowable.engine.impl.cmd.SetTaskDueDateCmd;
import org.flowable.engine.impl.cmd.SetTaskPriorityCmd;
import org.flowable.engine.impl.cmd.SetTaskVariablesCmd;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.runtime.DataObject;
import org.flowable.engine.task.Attachment;
import org.flowable.engine.task.Comment;
import org.flowable.engine.task.Event;
import org.flowable.form.model.FormModel;
import org.flowable.identitylink.service.IdentityLink;
import org.flowable.identitylink.service.IdentityLinkType;
import org.flowable.task.service.NativeTaskQuery;
import org.flowable.task.service.Task;
import org.flowable.task.service.TaskQuery;
import org.flowable.task.service.impl.NativeTaskQueryImpl;
import org.flowable.task.service.impl.TaskQueryImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {

    public TaskServiceImpl() {

    }

    public TaskServiceImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
        super(processEngineConfiguration);
    }

    public Task newTask() {
        return newTask(null);
    }

    public Task newTask(String taskId) {
        return commandExecutor.execute(new NewTaskCmd(taskId));
    }

    public void saveTask(Task task) {
        commandExecutor.execute(new SaveTaskCmd(task));
    }

    public void deleteTask(String taskId) {
        commandExecutor.execute(new DeleteTaskCmd(taskId, null, false));
    }

    public void deleteTasks(Collection<String> taskIds) {
        commandExecutor.execute(new DeleteTaskCmd(taskIds, null, false));
    }

    public void deleteTask(String taskId, boolean cascade) {
        commandExecutor.execute(new DeleteTaskCmd(taskId, null, cascade));
    }

    public void deleteTasks(Collection<String> taskIds, boolean cascade) {
        commandExecutor.execute(new DeleteTaskCmd(taskIds, null, cascade));
    }

    @Override
    public void deleteTask(String taskId, String deleteReason) {
        commandExecutor.execute(new DeleteTaskCmd(taskId, deleteReason, false));
    }

    @Override
    public void deleteTasks(Collection<String> taskIds, String deleteReason) {
        commandExecutor.execute(new DeleteTaskCmd(taskIds, deleteReason, false));
    }

    public void setAssignee(String taskId, String userId) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, AddIdentityLinkCmd.IDENTITY_USER, IdentityLinkType.ASSIGNEE));
    }

    public void setOwner(String taskId, String userId) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, AddIdentityLinkCmd.IDENTITY_USER, IdentityLinkType.OWNER));
    }

    public void addCandidateUser(String taskId, String userId) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, AddIdentityLinkCmd.IDENTITY_USER, IdentityLinkType.CANDIDATE));
    }

    public void addCandidateGroup(String taskId, String groupId) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, groupId, AddIdentityLinkCmd.IDENTITY_GROUP, IdentityLinkType.CANDIDATE));
    }

    public void addUserIdentityLink(String taskId, String userId, String identityLinkType) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, AddIdentityLinkCmd.IDENTITY_USER, identityLinkType));
    }

    public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
        commandExecutor.execute(new AddIdentityLinkCmd(taskId, groupId, AddIdentityLinkCmd.IDENTITY_GROUP, identityLinkType));
    }

    public void deleteCandidateGroup(String taskId, String groupId) {
        commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, IdentityLinkType.CANDIDATE));
    }

    public void deleteCandidateUser(String taskId, String userId) {
        commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, IdentityLinkType.CANDIDATE));
    }

    public void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
        commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, identityLinkType));
    }

    public void deleteUserIdentityLink(String taskId, String userId, String identityLinkType) {
        commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, identityLinkType));
    }

    public List<IdentityLink> getIdentityLinksForTask(String taskId) {
        return commandExecutor.execute(new GetIdentityLinksForTaskCmd(taskId));
    }

    public void claim(String taskId, String userId) {
        commandExecutor.execute(new ClaimTaskCmd(taskId, userId));
    }

    public void unclaim(String taskId) {
        commandExecutor.execute(new ClaimTaskCmd(taskId, null));
    }

    public void complete(String taskId) {
        commandExecutor.execute(new CompleteTaskCmd(taskId, null));
    }

    public void complete(String taskId, Map<String, Object> variables) {
        commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
    }

    @Override
    public void complete(String taskId, Map<String, Object> variables, Map<String, Object> transientVariables) {
        commandExecutor.execute(new CompleteTaskCmd(taskId, variables, transientVariables));
    }

    public void complete(String taskId, Map<String, Object> variables, boolean localScope) {
        commandExecutor.execute(new CompleteTaskCmd(taskId, variables, localScope));
    }

    public void completeTaskWithForm(String taskId, String formDefinitionId, String outcome, Map<String, Object> variables) {
        commandExecutor.execute(new CompleteTaskWithFormCmd(taskId, formDefinitionId, outcome, variables));
    }

    public void completeTaskWithForm(String taskId, String formDefinitionId, String outcome,
            Map<String, Object> variables, Map<String, Object> transientVariables) {

        commandExecutor.execute(new CompleteTaskWithFormCmd(taskId, formDefinitionId, outcome, variables, transientVariables));
    }

    public void completeTaskWithForm(String taskId, String formDefinitionId, String outcome,
            Map<String, Object> variables, boolean localScope) {

        commandExecutor.execute(new CompleteTaskWithFormCmd(taskId, formDefinitionId, outcome, variables, localScope));
    }

    public FormModel getTaskFormModel(String taskId) {
        return commandExecutor.execute(new GetTaskFormModelCmd(taskId));
    }

    public void delegateTask(String taskId, String userId) {
        commandExecutor.execute(new DelegateTaskCmd(taskId, userId));
    }

    public void resolveTask(String taskId) {
        commandExecutor.execute(new ResolveTaskCmd(taskId, null));
    }

    public void resolveTask(String taskId, Map<String, Object> variables) {
        commandExecutor.execute(new ResolveTaskCmd(taskId, variables));
    }

    @Override
    public void resolveTask(String taskId, Map<String, Object> variables, Map<String, Object> transientVariables) {
        commandExecutor.execute(new ResolveTaskCmd(taskId, variables, transientVariables));
    }

    public void setPriority(String taskId, int priority) {
        commandExecutor.execute(new SetTaskPriorityCmd(taskId, priority));
    }

    public void setDueDate(String taskId, Date dueDate) {
        commandExecutor.execute(new SetTaskDueDateCmd(taskId, dueDate));
    }

    public TaskQuery createTaskQuery() {
        return new TaskQueryImpl(commandExecutor, processEngineConfiguration.getDatabaseType());
    }

    public NativeTaskQuery createNativeTaskQuery() {
        return new NativeTaskQueryImpl(commandExecutor);
    }

    public Map<String, Object> getVariables(String taskId) {
        return commandExecutor.execute(new GetTaskVariablesCmd(taskId, null, false));
    }

    public Map<String, Object> getVariablesLocal(String taskId) {
        return commandExecutor.execute(new GetTaskVariablesCmd(taskId, null, true));
    }

    public Map<String, Object> getVariables(String taskId, Collection<String> variableNames) {
        return commandExecutor.execute(new GetTaskVariablesCmd(taskId, variableNames, false));
    }

    public Map<String, Object> getVariablesLocal(String taskId, Collection<String> variableNames) {
        return commandExecutor.execute(new GetTaskVariablesCmd(taskId, variableNames, true));
    }

    public Object getVariable(String taskId, String variableName) {
        return commandExecutor.execute(new GetTaskVariableCmd(taskId, variableName, false));
    }

    @Override
    public <T> T getVariable(String taskId, String variableName, Class<T> variableClass) {
        return variableClass.cast(getVariable(taskId, variableName));
    }

    public boolean hasVariable(String taskId, String variableName) {
        return commandExecutor.execute(new HasTaskVariableCmd(taskId, variableName, false));
    }

    public Object getVariableLocal(String taskId, String variableName) {
        return commandExecutor.execute(new GetTaskVariableCmd(taskId, variableName, true));
    }

    @Override
    public <T> T getVariableLocal(String taskId, String variableName, Class<T> variableClass) {
        return variableClass.cast(getVariableLocal(taskId, variableName));
    }

    public List<VariableInstance> getVariableInstancesLocalByTaskIds(Set<String> taskIds) {
        return commandExecutor.execute(new GetTasksLocalVariablesCmd(taskIds));
    }

    public boolean hasVariableLocal(String taskId, String variableName) {
        return commandExecutor.execute(new HasTaskVariableCmd(taskId, variableName, true));
    }

    public void setVariable(String taskId, String variableName, Object value) {
        if (variableName == null) {
            throw new FlowableIllegalArgumentException("variableName is null");
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(variableName, value);
        commandExecutor.execute(new SetTaskVariablesCmd(taskId, variables, false));
    }

    public void setVariableLocal(String taskId, String variableName, Object value) {
        if (variableName == null) {
            throw new FlowableIllegalArgumentException("variableName is null");
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(variableName, value);
        commandExecutor.execute(new SetTaskVariablesCmd(taskId, variables, true));
    }

    public void setVariables(String taskId, Map<String, ? extends Object> variables) {
        commandExecutor.execute(new SetTaskVariablesCmd(taskId, variables, false));
    }

    public void setVariablesLocal(String taskId, Map<String, ? extends Object> variables) {
        commandExecutor.execute(new SetTaskVariablesCmd(taskId, variables, true));
    }

    public void removeVariable(String taskId, String variableName) {
        Collection<String> variableNames = new ArrayList<>();
        variableNames.add(variableName);
        commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
    }

    public void removeVariableLocal(String taskId, String variableName) {
        Collection<String> variableNames = new ArrayList<>(1);
        variableNames.add(variableName);
        commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
    }

    public void removeVariables(String taskId, Collection<String> variableNames) {
        commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
    }

    public void removeVariablesLocal(String taskId, Collection<String> variableNames) {
        commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
    }

    public Comment addComment(String taskId, String processInstance, String message) {
        return commandExecutor.execute(new AddCommentCmd(taskId, processInstance, message));
    }

    public Comment addComment(String taskId, String processInstance, String type, String message) {
        return commandExecutor.execute(new AddCommentCmd(taskId, processInstance, type, message));
    }
    
    public void saveComment(Comment comment) {
        commandExecutor.execute(new SaveCommentCmd((CommentEntity) comment));
    }

    @Override
    public Comment getComment(String commentId) {
        return commandExecutor.execute(new GetCommentCmd(commentId));
    }

    @Override
    public Event getEvent(String eventId) {
        return commandExecutor.execute(new GetTaskEventCmd(eventId));
    }

    public List<Comment> getTaskComments(String taskId) {
        return commandExecutor.execute(new GetTaskCommentsCmd(taskId));
    }

    public List<Comment> getTaskComments(String taskId, String type) {
        return commandExecutor.execute(new GetTaskCommentsByTypeCmd(taskId, type));
    }

    public List<Comment> getCommentsByType(String type) {
        return commandExecutor.execute(new GetTypeCommentsCmd(type));
    }

    public List<Event> getTaskEvents(String taskId) {
        return commandExecutor.execute(new GetTaskEventsCmd(taskId));
    }

    public List<Comment> getProcessInstanceComments(String processInstanceId) {
        return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId));
    }

    public List<Comment> getProcessInstanceComments(String processInstanceId, String type) {
        return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId, type));
    }

    public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content) {
        return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content, null));
    }

    public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url) {
        return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, null, url));
    }

    public InputStream getAttachmentContent(String attachmentId) {
        return commandExecutor.execute(new GetAttachmentContentCmd(attachmentId));
    }

    public void deleteAttachment(String attachmentId) {
        commandExecutor.execute(new DeleteAttachmentCmd(attachmentId));
    }

    public void deleteComments(String taskId, String processInstanceId) {
        commandExecutor.execute(new DeleteCommentCmd(taskId, processInstanceId, null));
    }

    @Override
    public void deleteComment(String commentId) {
        commandExecutor.execute(new DeleteCommentCmd(null, null, commentId));
    }

    public Attachment getAttachment(String attachmentId) {
        return commandExecutor.execute(new GetAttachmentCmd(attachmentId));
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getTaskAttachments(String taskId) {
        return (List<Attachment>) commandExecutor.execute(new GetTaskAttachmentsCmd(taskId));
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> getProcessInstanceAttachments(String processInstanceId) {
        return (List<Attachment>) commandExecutor.execute(new GetProcessInstanceAttachmentsCmd(processInstanceId));
    }

    public void saveAttachment(Attachment attachment) {
        commandExecutor.execute(new SaveAttachmentCmd(attachment));
    }

    public List<Task> getSubTasks(String parentTaskId) {
        return commandExecutor.execute(new GetSubTasksCmd(parentTaskId));
    }

    @Override
    public VariableInstance getVariableInstance(String taskId, String variableName) {
        return commandExecutor.execute(new GetTaskVariableInstanceCmd(taskId, variableName, false));
    }

    @Override
    public VariableInstance getVariableInstanceLocal(String taskId, String variableName) {
        return commandExecutor.execute(new GetTaskVariableInstanceCmd(taskId, variableName, true));
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(String taskId) {
        return commandExecutor.execute(new GetTaskVariableInstancesCmd(taskId, null, false));
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(String taskId, Collection<String> variableNames) {
        return commandExecutor.execute(new GetTaskVariableInstancesCmd(taskId, variableNames, false));
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(String taskId) {
        return commandExecutor.execute(new GetTaskVariableInstancesCmd(taskId, null, true));
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(String taskId, Collection<String> variableNames) {
        return commandExecutor.execute(new GetTaskVariableInstancesCmd(taskId, variableNames, true));
    }

    @Override
    public Map<String, DataObject> getDataObjects(String taskId) {
        return commandExecutor.execute(new GetTaskDataObjectsCmd(taskId, null));
    }

    @Override
    public Map<String, DataObject> getDataObjects(String taskId, String locale, boolean withLocalizationFallback) {
        return commandExecutor.execute(new GetTaskDataObjectsCmd(taskId, null, locale, withLocalizationFallback));
    }

    @Override
    public Map<String, DataObject> getDataObjects(String taskId, Collection<String> dataObjectNames) {
        return commandExecutor.execute(new GetTaskDataObjectsCmd(taskId, dataObjectNames));
    }

    @Override
    public Map<String, DataObject> getDataObjects(String taskId, Collection<String> dataObjectNames, String locale, boolean withLocalizationFallback) {
        return commandExecutor.execute(new GetTaskDataObjectsCmd(taskId, dataObjectNames, locale, withLocalizationFallback));
    }

    @Override
    public DataObject getDataObject(String taskId, String dataObject) {
        return commandExecutor.execute(new GetTaskDataObjectCmd(taskId, dataObject));
    }

    @Override
    public DataObject getDataObject(String taskId, String dataObjectName, String locale, boolean withLocalizationFallback) {
        return commandExecutor.execute(new GetTaskDataObjectCmd(taskId, dataObjectName, locale, withLocalizationFallback));
    }

}
