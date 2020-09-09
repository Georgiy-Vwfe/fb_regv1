package com.sixhands.domain;

import javax.persistence.*;

@Entity
@Table(name = "user_project_exp")
public class UserProjectExp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uuid;

    private Long user_uuid;
    private Long project_uuid;
    private String position;
    private String role;
    private String skills;
    private String tools;
    private String duties;
    //Already in project
    //private String industry;

    private String custom_description;
    private boolean project_creator = false;

    public UserProjectExp safeAssignProperties(UserProjectExp unsafe){
        role = unsafe.getRole();
        duties = unsafe.getDuties();
        tools = unsafe.getTools();
        skills = unsafe.getSkills();
        position = unsafe.getPosition();
        return this;
    }

    //#region getters/setters
    public Long getUser_uuid() {
        return user_uuid;
    }

    public void setUser_uuid(Long user_uuid) {
        this.user_uuid = user_uuid;
    }

    public Long getProject_uuid() {
        return project_uuid;
    }

    public void setProject_uuid(Long project_uuid) {
        this.project_uuid = project_uuid;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getTools() {
        return tools;
    }

    public void setTools(String tools) {
        this.tools = tools;
    }

    //public String getIndustry() { return industry; }

    //public void setIndustry(String industry) { this.industry = industry; }

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public boolean isProject_creator() {
        return project_creator;
    }

    public void setProject_creator(boolean project_creator) {
        this.project_creator = project_creator;
    }

    public String getCustom_description() {
        return custom_description;
    }

    public void setCustom_description(String custom_description) {
        this.custom_description = custom_description;
    }

    public String getDuties() {
        return duties;
    }

    public void setDuties(String duties) {
        this.duties = duties;
    }
    //#endregion
}
