package ch.usi.dag.dislreserver.msg.classinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.ShadowClassTable;


public final class ClassInfoHandler implements RequestHandler {

    @Override
    public void handle (
        final DataInputStream is, final DataOutputStream os,
        final boolean debug
    ) throws DiSLREServerException {

        try {
            final long netReference = is.readLong ();
            final String typeDescriptor = is.readUTF ();
            final String classGenericStr = is.readUTF ();
            final long classLoaderNetReference = is.readLong ();
            final long superclassNetReference = is.readLong ();

            ShadowClassTable.registerClass (
                netReference, typeDescriptor, classGenericStr,
                classLoaderNetReference, superclassNetReference
            );

        } catch (final IOException e) {
            throw new DiSLREServerException (e);
        }
    }


    @Override
    public void exit () {
        // do nothing
    }

}
