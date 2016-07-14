public class MultipleFilesProcessor {

    public static void main(String[] args) throws Exception {
        PathFilesProcessor processor = new PathFilesProcessor(args[0]);
        processor.run();
    }
}