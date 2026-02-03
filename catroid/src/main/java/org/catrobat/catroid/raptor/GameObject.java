package org.catrobat.catroid.raptor;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    public String id;
    public String name;
    public boolean isActive = true;
    public TransformComponent transform = new TransformComponent();
    public List<Component> components = new ArrayList<>();

    public String parentId = null;
    public List<String> childrenIds = new ArrayList<>();

    public GameObject() {}

    public GameObject(String name) {
        this.id = name;
        this.name = name;
    }

    public boolean addComponent(Component component) {
        if (component instanceof KeyframeComponent) {
            KeyframeComponent keyframeComp = (KeyframeComponent) component;
            if (keyframeComp.keyframes.isEmpty()) {
                KeyframeData initialFrame = new KeyframeData();
                initialFrame.time = 0f;
                initialFrame.position.set(this.transform.position);
                initialFrame.rotation.set(this.transform.rotation);
                initialFrame.scale.set(this.transform.scale);
                keyframeComp.keyframes.add(initialFrame);
            }
        }
        components.add(component);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) {
                return (T) c;
            }
        }
        return null;
    }

    public <T extends Component> boolean hasComponent(Class<T> type) {
        return getComponent(type) != null;
    }

    public <T extends Component> List<T> getComponents(Class<T> type) {
        List<T> foundComponents = new ArrayList<>();
        for (Component c : components) {
            if (type.isInstance(c)) {
                foundComponents.add((T) c);
            }
        }
        return foundComponents;
    }
}