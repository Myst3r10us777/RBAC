public record Permission(String name, String resource, String description) {
    public Permission(String name, String resource, String description){
        if (!name.contains(" ")){
            this.name = name.toUpperCase();
        } else {
            throw new IllegalArgumentException("Name содержит пробел!");
        }
        this.resource = resource.toLowerCase();
        if (description.isEmpty()){
            throw new IllegalArgumentException("Description пустой!");
        } else{
            this.description = description;
        }
    }

    boolean matches(String namePattern, String resourcePattern){
        if (name.contains(namePattern) && resource.contains(resourcePattern)){
            return true;
        } else {
            return false;
        }
    }

    public String format(){
        String info = name + " on " + resource + ": " + description;
        return info;
    }
}
