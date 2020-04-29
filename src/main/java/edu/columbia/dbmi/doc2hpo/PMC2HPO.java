package edu.columbia.dbmi.doc2hpo;

import com.alibaba.fastjson.JSON;
import edu.columbia.dbmi.doc2hpo.pojo.ParsingResults;
import edu.columbia.dbmi.doc2hpo.service.ACTrieParser;
import edu.columbia.dbmi.doc2hpo.util.FileUtil;

import java.io.FileNotFoundException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Integer.*;


class ParseWorker extends Thread {
    public final static String PoisonPill = "POISON_PILL";
    private final BlockingQueue<String> queue;
    private final ACTrieParser parser;

    ParseWorker(BlockingQueue<String> queue) throws FileNotFoundException {
        this.queue = queue;
        this.parser = new ACTrieParser();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String path = queue.take();

                if (path.equals(ParseWorker.PoisonPill)) {
                    return;
                }

                String content= FileUtil.Readfile(path);
                List<ParsingResults> results = null;
                try {
                     results = this.parser.parse(this.parser, content,false,false);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (null != results){
                    FileUtil.write2File(path + ".json", JSON.toJSONString(results));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class PMC2HPO {
    public static void main(String[] args) throws Exception {
        int processNum = parseInt(args[0]);
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(processNum * 5);

        ExecutorService pool = Executors.newFixedThreadPool(processNum);

        Collections.nCopies(processNum, queue).forEach(q -> {
            try {
                pool.execute(new ParseWorker(queue));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(args[1]), args[2])) {
            stream.forEach(path -> {
                try {
                    queue.put(path.toString());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Collections.nCopies(processNum, ParseWorker.PoisonPill).forEach(p -> {
            try {
                queue.put(p);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        pool.shutdown();
    }
}
