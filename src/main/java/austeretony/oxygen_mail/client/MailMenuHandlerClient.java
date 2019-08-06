package austeretony.oxygen_mail.client;

import java.util.Set;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.sync.gui.api.IAdvancedGUIHandlerClient;
import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.Message;
import net.minecraft.network.PacketBuffer;

public class MailMenuHandlerClient implements IAdvancedGUIHandlerClient<Message> {

    @Override
    public void open() {
        ClientReference.displayGuiScreen(new MailMenuGUIScreen());
    }

    @Override
    public OxygenNetwork getNetwork() {
        return MailMain.network();
    }

    @Override
    public Set<Long> getIdentifiers() {
        return MailManagerClient.instance().getMessagesIds();
    }

    @Override
    public Message getEntry(long entryId) {
        return MailManagerClient.instance().getMessage(entryId);
    }

    @Override
    public void clearData() {
        MailManagerClient.instance().reset();
    }

    @Override
    public void addValidEntry(Message entry) {
        MailManagerClient.instance().addMessage(entry);
    }

    @Override
    public void readEntries(PacketBuffer buffer, int amount) {
        for (int i = 0; i < amount; i++)
            MailManagerClient.instance().addMessage(Message.read(buffer));
        MailManagerClient.instance().saveMail();
    }
}