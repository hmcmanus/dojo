package com.mcmanus.samples.xml;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Sandwich
{
    private String name;
    private List<Filling> fillings;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Filling> getFillings()
    {
        return fillings;
    }

    public void setFillings(List<Filling> fillings)
    {
        this.fillings = fillings;
    }


}
