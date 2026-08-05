package com.lifepilot.agent;

import com.lifepilot.agent.dto.AgentAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 记录单次 Agent 调用期间由工具产生的计划草案动作。
 */
@Component
public class PlanPreviewActionContext {

    private final ThreadLocal<List<AgentAction>> actions = ThreadLocal.withInitial(ArrayList::new);

    public void begin() {
        actions.set(new ArrayList<>());
    }

    public void recordPlanPreview(UUID id, String label) {
        actions.get().add(new AgentAction("PLAN_PREVIEW", id, label));
    }

    public List<AgentAction> currentActions() {
        return List.copyOf(actions.get());
    }

    public void clear() {
        actions.remove();
    }
}
