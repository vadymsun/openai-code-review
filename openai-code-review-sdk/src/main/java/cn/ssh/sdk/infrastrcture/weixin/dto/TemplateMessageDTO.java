package cn.ssh.sdk.infrastrcture.weixin.dto;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessageDTO {

    private String touser;
    private String templateId;
    private String url;
    private Map<String, Map<String, String>> data;

    public TemplateMessageDTO(String touser, String templateId, String url, Map<String, Map<String, String>> data) {
        this.touser = touser;
        this.templateId = templateId;
        this.url = url;
        this.data = data;
    }
    public static void put(Map<String, Map<String, String>> data, String key, String value) {
        data.put(key, new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    public  enum TemplateKey {
        PROJECT("project","项目名称"),
        AUTHOR("author","提交者"),
        MESSAGE("message","提交信息"),
        ;

        private String code;
        private String desc;

        TemplateKey(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }



}
