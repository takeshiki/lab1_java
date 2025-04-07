package lab.src;

public interface JsonSerializable<T> {
    String toJson();
    T fromJson(String json);
}
