package org.springframework.samples.petclinic.snapshot;

import org.crac.Core;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/snapshot")
public class SnapshotController {

	@PostMapping("/create")
	public ResponseEntity<String> createSnapshot() {

		// Run checkpoint in a separate thread to avoid blocking the HTTP thread
		new Thread(() -> {
			try {
				System.out.println("[CRaC] Creating snapshot now …");
				for (var thread : Thread.getAllStackTraces().keySet()) {
					if (thread.getName().toLowerCase().contains("socket")) {
						System.out.println("Possible socket thread: " + thread);
					}
				}
				Core.checkpointRestore(); // JVM will write snapshot & exit
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}, "crac-checkpoint-thread").start();

		return ResponseEntity.accepted().body("Snapshot creation initiated. Server will exit if checkpoint succeeds.");
	}

}
