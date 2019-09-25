package ru.zaxar163.repupdater;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gitlab4j.api.GitLabApi;

public class Main {
    public static void main(String[] args) throws Throwable {
    	if (args.length < 3) {
    		System.out.println("Usage: <URL> <login> <password>");
    		return;
    	}
    	GitLabApi api = GitLabApi.oauth2Login(args[0], args[1], args[2]);
    	Path core = Paths.get("cloned");
    	api.getProjectApi().getProjects(100).lazyStream().limit(1000).forEach(e -> {
    		try {
    			Path group = core.resolveSibling(e.getNamespace().getFullPath());
    			if (group != null && !Files.isDirectory(group))
    				Files.createDirectories(group);
    			Path sum = group.resolve(e.getName());
    			ProcessBuilder pb = new ProcessBuilder();
    			
    			if (Files.isDirectory(sum)) {
    				pb.command("git", "fetch");
    				pb.directory(sum.toFile());
    			} else {
    				pb.command("git", "clone", e.getSshUrlToRepo(), e.getName());
        			pb.directory(group.toFile());
        			Thread.sleep(1000);
    			}
    			pb.inheritIO();
    			pb.start().waitFor();
    		} catch (Throwable t) {
    			throw new RuntimeException(t);
    		}
    	});
    }
}
