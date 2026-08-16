package killguard.config;

import java.util.ArrayList;
import java.util.List;

public class Config
{
    public List<String> protectedEntityTags = new ArrayList<>(List.of("killguard"));
    public List<String> protectedEntityTypes = new ArrayList<>();
}
