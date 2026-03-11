import java.lang.reflect.Array;
import java.util.*;

public class UserManager implements Repository<User> {
    Map<String, User> users = new HashMap<>();

    public Optional<User> findByUsername(String username){
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email){
        for (User user : users.values()) {
            if (email.equals(user.email())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> findByFilter(UserFilter filter){
        List<User> result = new ArrayList<>();

        for (User user : users.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }

        return result;
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter){
        List<User> result = new ArrayList<>();

        for (User user : users.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }
        Collections.sort(result, sorter);
        return result;
    }

    public boolean exists(String username){
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail){
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if(exists(username)){
            User user = User.create(username, newFullName, newEmail);
            users.put(username, user);
        }
    }

    @Override
    public void add(User user){
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (user.username() == null || user.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (users.containsKey(user.username())){
            System.out.println("Element: " + user.format() + " already in Map!");
        } else {
            users.put(user.username(), user);
        }
    }

    @Override
    public boolean remove(User user){
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id){
        return findByUsername(id);
    }

    @Override
    public List<User> findAll(){
        List<User> userlist = new ArrayList<>();
        for (User user : users.values()){
            userlist.add(user);
        }
        return userlist;
    }

    @Override
    public int count(){
        return users.size();
    }

    @Override
    public void clear(){
        users.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UserManager that = (UserManager) obj;
        return Objects.equals(users, that.users);
    }

    public int hashCode() {
        return Objects.hashCode(users);
    }
}
