package eakimov.ftp.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListCommandAnswer {
    private final List<DirectoryContent> contents;

    public ListCommandAnswer(List<DirectoryContent> contents) {
        this.contents = contents;
    }

    public ListCommandAnswer(DataInputStream inputStream) throws IOException {
        contents = new ArrayList<>();
        final int size = inputStream.readInt();
        for (int i = 0; i < size; i++) {
            final String name = inputStream.readUTF();
            final boolean isDir = inputStream.readBoolean();
            contents.add(new DirectoryContent(name, isDir));
        }
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(contents.size());
        for (DirectoryContent item : contents) {
            outputStream.writeUTF(item.getName());
            outputStream.writeBoolean(item.isDir());
        }
        outputStream.flush();
    }

    public List<DirectoryContent> getContents() {
        return contents;
    }

    public static class DirectoryContent {
        private final String name;
        private final boolean isDir;

        public DirectoryContent(String name, boolean isDir) {
            this.name = name;
            this.isDir = isDir;
        }

        public String getName() {
            return name;
        }

        public boolean isDir() {
            return isDir;
        }

        @Override
        public boolean equals(Object obj) {  // for tests
            if (obj instanceof DirectoryContent) {
                final DirectoryContent that = (DirectoryContent) obj;
                return getName().equals(that.getName()) && isDir() == that.isDir();
            }
            return false;
        }

        @Override
        public int hashCode() {  // for tests
            return name.hashCode() ^ (isDir ? 1 : 0);
        }
    }
}
