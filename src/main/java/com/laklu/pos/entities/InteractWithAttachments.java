package com.laklu.pos.entities;

import java.util.Set;

public interface InteractWithAttachments<T> {
    Set<Attachment> getAttachments();

    void addAttachment(Attachment ...attachment);

    void setAttachments(Set<Attachment> attachments);

    T getId();
}
