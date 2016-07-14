package im.actor.core.modules.encryption;

import im.actor.core.api.updates.UpdateEncryptedPackage;
import im.actor.core.api.updates.UpdatePublicKeyGroupAdded;
import im.actor.core.api.updates.UpdatePublicKeyGroupRemoved;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class EncryptionProcessor extends AbsModule implements SequenceProcessor {

    public EncryptionProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdatePublicKeyGroupAdded) {
            UpdatePublicKeyGroupAdded groupAdded = (UpdatePublicKeyGroupAdded) update;
            return context().getEncryption()
                    .getKeyManager()
                    .onKeyGroupAdded(groupAdded.getUid(), groupAdded.getKeyGroup());
        } else if (update instanceof UpdatePublicKeyGroupRemoved) {
            UpdatePublicKeyGroupRemoved groupRemoved = (UpdatePublicKeyGroupRemoved) update;
            return context().getEncryption()
                    .getKeyManager()
                    .onKeyGroupRemoved(groupRemoved.getUid(), groupRemoved.getKeyGroupId());
        } else if (update instanceof UpdateEncryptedPackage) {
            UpdateEncryptedPackage encryptedPackage = (UpdateEncryptedPackage) update;
            return context().getEncryption()
                    .decrypt(encryptedPackage.getSenderId(), encryptedPackage.getEncryptedBox())
                    .flatMap(message -> context().getEncryption()
                            .onUpdate(
                                    encryptedPackage.getSenderId(),
                                    encryptedPackage.getDate(),
                                    message))
                    .fallback(e -> Promise.success(null));
        }
        return null;
    }
}
