package lab.src;

public interface JsonSerializable {
    String toJson();
    JsonSerializable fromJson(String json);
}
