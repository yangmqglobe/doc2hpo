package edu.columbia.dbmi.doc2hpo;

import com.alibaba.fastjson.JSON;
import edu.columbia.dbmi.doc2hpo.pojo.ParsingResults;
import edu.columbia.dbmi.doc2hpo.service.ACTrieParser;

import java.io.*;
import java.util.List;

public class PMC2HPO {
    public static void main(String[] args) throws Exception {
        ACTrieParser rbp = new ACTrieParser();
        StringBuilder input = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        while (line != null){
            input.append(line);
            line = br.readLine();
        }
        String content = input.toString();
        List<ParsingResults> results=rbp.parse(rbp, content,false,false);
        System.out.println(JSON.toJSONString(results, false));
    }
}
