package com.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SnapshotCli {

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			printHelp();
			return;
		}

		String cmd = args[0].toLowerCase();
		switch (cmd) {
			case "create" -> {
				if (args.length < 2) {
					System.err.println("Usage: create <serverBaseUrl>");
					return;
				}
				createSnapshot(args[1]);
			}
			case "restore" -> {
				if (args.length < 3) {
					System.err.println("Usage: restore <checkpointDir> <serverJarPath>");
					return;
				}
				restore(args[1], args[2]);
			}
			default -> printHelp();
		}
	}

	private static void printHelp() {
		System.out.println("""
				Snapshot CLI

				Commands:
				  create  <serverBaseUrl>
				    Send POST /snapshot/create to the running PetClinic server.

				  restore <checkpointDir> <serverJarPath>
				    Kill existing server (if PID file exists) and start a restored instance.

				Examples:
				  java -jar snapshot-cli.jar create http://localhost:8080
				  java -jar snapshot-cli.jar restore ./checkpoints target/spring-petclinic.jar
				""");
	}

	// --- CREATE ---

	private static void createSnapshot(String serverBaseUrl) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest req = HttpRequest.newBuilder()
			.uri(URI.create(serverBaseUrl + "/snapshot/create"))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();

		System.out.println("[CLI] Requesting snapshot creation from " + serverBaseUrl);
		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
		System.out.println("[CLI] HTTP " + resp.statusCode() + ": " + resp.body());
	}

	// --- RESTORE ---

	private static void restore(String checkpointDir, String serverJarPath) throws IOException {
		System.out.println("[CLI] Attempting to stop current server using application.pid …");
		stopServerIfRunning();

		System.out.println("[CLI] Starting restored server from " + checkpointDir);
		ProcessBuilder pb = new ProcessBuilder("java", "-XX:CRaCRestoreFrom=" + checkpointDir, "-jar", serverJarPath);
		pb.inheritIO();
		pb.start(); // detached; caller can choose to wait on it if needed
	}

	private static void stopServerIfRunning() {
		Path pidPath = Path.of("application.pid");
		if (!Files.exists(pidPath)) {
			System.out.println("[CLI] No PID file found; assuming server is not running.");
			return;
		}

		try {
			String content = Files.readString(pidPath).trim();
			long pid = Long.parseLong(content);
			Optional<ProcessHandle> ph = ProcessHandle.of(pid);
			if (ph.isPresent() && ph.get().isAlive()) {
				System.out.println("[CLI] Killing server process PID " + pid);
				ph.get().destroy(); // or destroyForcibly() if needed
			}
			else {
				System.out.println("[CLI] PID " + pid + " not alive.");
			}
		}
		catch (Exception e) {
			System.err.println("[CLI] Failed to parse/kill PID: " + e.getMessage());
		}

		try {
			Files.deleteIfExists(pidPath);
		}
		catch (IOException ignored) {
		}
	}

}
