package austeretony.oxygen_mail.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen.util.PacketBufferUtils;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import net.minecraft.network.PacketBuffer;

public class Mail {

    public static final int 
    MESSAGE_TITLE_MAX_LENGTH = 20,
    MESSAGE_MAX_LENGTH = 800;

    public static final UUID SYSTEM_UUID = UUID.fromString("d10d07f6-ae3c-4ec6-a055-1160c4cf848a");

    public final EnumMail type;

    private long messageId;

    public final UUID senderUUID;

    public final String senderName, subject, message;

    private int currency;

    private Parcel parcel;

    private boolean pending;

    public Mail(EnumMail type, UUID senderUUID, String senderName, String title, String message) {
        this.type = type;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        if (title.length() > MESSAGE_TITLE_MAX_LENGTH)
            title = title.substring(0, MESSAGE_TITLE_MAX_LENGTH);
        this.subject = title;
        if (message.length() > MESSAGE_MAX_LENGTH)
            message = message.substring(0, MESSAGE_MAX_LENGTH);
        this.message = message;
    }

    public long getId() {              
        return this.messageId;
    }

    public void setId(long messageId) {
        this.messageId = messageId;
    }

    public int getCurrency() {
        return this.currency;
    }

    public void setCurrency(int amount) {
        this.currency = amount;
    }

    public Parcel getParcel() {
        return this.parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public boolean isPending() {
        return this.pending;
    }  

    public void setPending(boolean flag) {
        this.pending = flag;
    }

    public boolean isExpired() {
        long expiresInHours = 0L;
        switch (this.type) {
        case SERVICE_LETTER:
            expiresInHours = MailConfig.SERVICE_LETTER_EXPIRE_TIME.getIntValue();
            break;
        case LETTER:
            expiresInHours = MailConfig.LETTER_EXPIRE_TIME.getIntValue();
            break;
        case SERVICE_REMITTANCE:
            expiresInHours = MailConfig.SERVICE_REMITTANCE_EXPIRE_TIME.getIntValue();
            break;
        case REMITTANCE:
            expiresInHours = MailConfig.REMITTANCE_EXPIRE_TIME.getIntValue();
            break;
        case SERVICE_PACKAGE:
            expiresInHours = MailConfig.SERVICE_PACKAGE_EXPIRE_TIME.getIntValue();
            break;
        case PACKAGE:
            expiresInHours = MailConfig.PACKAGE_EXPIRE_TIME.getIntValue();
            break;
        case PACKAGE_WITH_COD:
            expiresInHours = MailConfig.PACKAGE_WITH_COD_EXPIRE_TIME.getIntValue();
            break;  
        }
        if (expiresInHours < 0L)
            return false;
        return System.currentTimeMillis() - this.messageId > expiresInHours * 3_600_000L;
    }

    public boolean hasAttachment() {
        switch (this.type) {
        case SERVICE_REMITTANCE:
        case REMITTANCE:
        case SERVICE_PACKAGE:
        case PACKAGE:
        case PACKAGE_WITH_COD:
            return true;
        default:
            return false;
        }
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((byte) this.type.ordinal(), bos);
        StreamUtils.write(this.senderUUID, bos);
        StreamUtils.write(this.senderName, bos);
        StreamUtils.write(this.subject, bos);
        StreamUtils.write(this.message, bos);

        StreamUtils.write(this.messageId, bos);
        StreamUtils.write(this.currency, bos);
        StreamUtils.write(this.pending, bos);

        StreamUtils.write(this.parcel == null ? false : true, bos);
        if (this.parcel != null)
            this.parcel.write(bos);
    }

    public static Mail read(BufferedInputStream bis) throws IOException {
        Mail message = new Mail(
                EnumMail.values()[StreamUtils.readByte(bis)],
                StreamUtils.readUUID(bis),
                StreamUtils.readString(bis), 
                StreamUtils.readString(bis),
                StreamUtils.readString(bis));

        message.messageId = StreamUtils.readLong(bis);
        message.currency = StreamUtils.readInt(bis);
        message.pending = StreamUtils.readBoolean(bis);

        if (StreamUtils.readBoolean(bis))
            message.parcel = Parcel.read(bis);

        return message;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeByte(this.type.ordinal());
        PacketBufferUtils.writeUUID(this.senderUUID, buffer);
        PacketBufferUtils.writeString(this.senderName, buffer);
        PacketBufferUtils.writeString(this.subject, buffer);
        PacketBufferUtils.writeString(this.message, buffer);

        buffer.writeLong(this.messageId);
        buffer.writeInt(this.currency);
        buffer.writeBoolean(this.pending);

        buffer.writeBoolean(this.parcel == null ? false : true);
        if (this.parcel != null)
            this.parcel.write(buffer);
    }

    public static Mail read(PacketBuffer buffer) {
        Mail message = new Mail(
                EnumMail.values()[buffer.readByte()], 
                PacketBufferUtils.readUUID(buffer),
                PacketBufferUtils.readString(buffer),
                PacketBufferUtils.readString(buffer),
                PacketBufferUtils.readString(buffer));

        message.messageId = buffer.readLong();
        message.currency = buffer.readInt();
        message.pending = buffer.readBoolean();

        if (buffer.readBoolean())
            message.parcel = Parcel.read(buffer);

        return message;
    }
}