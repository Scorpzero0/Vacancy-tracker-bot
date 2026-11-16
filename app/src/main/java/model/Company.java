package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private String name;
    private String url;

    @Override
    public String toString() {
        return "<a href=\"" + url + "\">" + name + "</a>";
    }
}
