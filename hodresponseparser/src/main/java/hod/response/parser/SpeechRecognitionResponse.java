package hod.response.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vuv on 9/24/2015.
 */
public class SpeechRecognitionResponse {
    public ArrayList<Document> document;
    public class Document {
        public Integer offset;
        public String content;
        public Integer confidence;
        public Integer duration;
    }
}
