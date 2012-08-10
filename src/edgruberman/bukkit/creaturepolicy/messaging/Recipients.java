package edgruberman.bukkit.creaturepolicy.messaging;

import edgruberman.bukkit.creaturepolicy.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
