
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

class Elevator {
	private Direction currentDirection = Direction.UP;
	private State currentState = State.IDLE;
	private int currentFloor = 0;

	// the time is ms that it takes to switch floor
	public static int floorChangeTime = 1000;

	// Jobs which are be processed
	private TreeSet<Request> currentJobs = new TreeSet<>();
	private TreeSet<Request> currentFloors = new TreeSet<>();

	// Up direction jobs that are waiting to be processed
	private TreeSet<Request> upPendingJobs = new TreeSet<>();

	// Down direction jobs that are waiting to be processed
	private TreeSet<Request> downPendingJobs = new TreeSet<>();

	// Method starts the elevator
	public void startElevator() {
		System.out.println("The Elevator has started functioning");
		// I know this is frowned upon but i think it makes sense here because you can
		// go hours without someone pushing a button.
		while (true) {
			// Check if a job exists in the current que
			if (checkIfJob()) {
				// check if the current direction is set to up
				if (currentDirection == Direction.UP) {
					// Start processing the up direction commands. Pickup floor is lower then
					// dropoff
					processUpRequest();
					// check if the current que is empty
					if (currentJobs.isEmpty()) {

						// After finishing the current up direction que change to down direction to
						// pickup
						// If the down que is empty go back to the lowest request and pickup the people
						// to go back up
						// if Both are empty switch state to idle
						if (!downPendingJobs.isEmpty()) {
							addPendingDownJobsToCurrentJobs();
						} else if (!upPendingJobs.isEmpty()) {
							addPendingUpJobsToCurrentJobs();
						} else {
							currentState = State.IDLE;
							System.out.print("\nThe elevator is in Idle state");
						}
					}

				}
				if (currentDirection == Direction.DOWN) {
					processDownRequest();
					if (currentJobs.isEmpty()) {
						if (!upPendingJobs.isEmpty()) {
							addPendingUpJobsToCurrentJobs();
						} else if (!downPendingJobs.isEmpty()) {
							addPendingDownJobsToCurrentJobs();
						} else {
							currentState = State.IDLE;
							System.out.print("\nThe elevator is in Idle state");
						}

					}
				}
			}
		}
	}

	public boolean checkIfJob() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (currentJobs.isEmpty()) {
			return false;
		}
		return true;

	}

	private void processUpRequest() {
		// Set to floors that probably wont exist for simplicity
		int lowestFloorRequest = -9999;
		int highestFloor = 9999;
		// iterate through the list of current jobs looking for the lowest floor to pick
		// up on
		for (Request value : currentJobs) {
			if (lowestFloorRequest == -9999) {
				lowestFloorRequest = value.getExternalRequest().getSourceFloor();
			} else if (value.getExternalRequest().getSourceFloor() > lowestFloorRequest) {
				lowestFloorRequest = value.getExternalRequest().getSourceFloor();
			}

		}
		// pickup the lowest floor request in the current que
		moveToSourceFloor(lowestFloorRequest);

		// get the highest floor request in the current que
		for (Request value : currentJobs) {
			if (highestFloor == 9999) {
				highestFloor = value.getInternalRequest().getDestinationFloor();
			} else if (highestFloor < value.getInternalRequest().getDestinationFloor()) {
				highestFloor = value.getInternalRequest().getDestinationFloor();
			}

		}

		for (int i = lowestFloorRequest; i <= highestFloor; i++) {
			for (Request value : currentJobs) {
				if (highestFloor < value.getInternalRequest().getDestinationFloor()) {
					highestFloor = value.getInternalRequest().getDestinationFloor();
				}
			}
			for (Request value : currentJobs) {
				Set<Integer> floorsToStop = new HashSet<Integer>();
				floorsToStop.add(value.getInternalRequest().getDestinationFloor());
			}
			boolean trigger = false;
			currentFloor = i;

			List<Request> toRemoveList = new ArrayList<>();
			for (Request value : currentJobs) {
				if ((value.getExternalRequest().getSourceFloor() == currentFloor)
						&& (value.getInsideElevator() != true)) {
					System.out.print("\nThe elevator has reached " + currentFloor + " picking up");
					try {
						Thread.sleep(floorChangeTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.print("\nOpening Door -- ");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.print("Closing Door\n");
					value.setInsideElevator();
					trigger = true;
				}
				if (value.getInternalRequest().getDestinationFloor() == currentFloor) {
					toRemoveList.add(value);
					trigger = true;
				}
			}
			if (!toRemoveList.isEmpty()) {

				for (int j = 0; j < toRemoveList.size(); j++) {
					currentJobs.remove(toRemoveList.get(j));
				}
				System.out.print("\nThe elevator has reached " + currentFloor + " dropping off");
				try {
					Thread.sleep(floorChangeTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("\nOpening Door -- ");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("Closing Door\n");
				toRemoveList.clear();
			}

			if (trigger == false) {
				System.out.print("\nThe elevator has reached " + currentFloor);

			}

			try {
				Thread.sleep(floorChangeTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processDownRequest() {

		// Set to floors that probably wont exist for simplicity
		int lowestFloorRequest = -9999;
		int highestFloorRequest = 9999;
		// iterate through the list of current jobs looking for the highest floor to
		// pick up on
		for (Request value : currentJobs) {
			if (highestFloorRequest == 9999) {
				highestFloorRequest = value.getExternalRequest().getSourceFloor();
			} else if (value.getExternalRequest().getSourceFloor() < highestFloorRequest) {
				highestFloorRequest = value.getExternalRequest().getSourceFloor();
			}

		}
		// pickup the highest floor request in the current que
		moveToSourceFloor(highestFloorRequest);

		// get the lowest floor request(destination) in the current que
		for (Request value : currentJobs) {
			if (lowestFloorRequest == -9999) {
				lowestFloorRequest = value.getInternalRequest().getDestinationFloor();
			} else if (lowestFloorRequest > value.getInternalRequest().getDestinationFloor()) {
				lowestFloorRequest = value.getInternalRequest().getDestinationFloor();
			}

		}

		for (int i = highestFloorRequest; i >= lowestFloorRequest; i--) {
			for (Request value : currentJobs) {
				if (lowestFloorRequest < value.getInternalRequest().getDestinationFloor()) {
					lowestFloorRequest = value.getInternalRequest().getDestinationFloor();
				}

			}
			boolean trigger = false;
			currentFloor = i;
			List<Request> toRemoveList = new ArrayList<>();
			for (Request value : currentJobs) {
				if ((value.getExternalRequest().getSourceFloor() == currentFloor)
						&& (value.getInsideElevator() != true)) {
					System.out.print("\nThe elevator has reached " + currentFloor + " picking up");
					try {
						Thread.sleep(floorChangeTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.print("\nOpening Door -- ");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.print("Closing Door\n");
					value.setInsideElevator();
					trigger = true;
				}
				if (value.getInternalRequest().getDestinationFloor() == currentFloor) {
					toRemoveList.add(value);
					trigger = true;
				}
			}
			if (!toRemoveList.isEmpty()) {

				for (int j = 0; j < toRemoveList.size(); j++) {
					currentJobs.remove(toRemoveList.get(j));
				}
				System.out.print("\nThe elevator has reached " + currentFloor + " dropping off");
				try {
					Thread.sleep(floorChangeTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("\nOpening Door -- ");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("Closing Door\n");
				toRemoveList.clear();
			}

			if (trigger == false) {
				System.out.print("\nThe elevator has reached " + currentFloor);

			}
			try {
				Thread.sleep(floorChangeTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean checkIfNewJobCanBeProcessed(Request currentRequest) {
		if (checkIfJob()) {
			if (currentDirection == Direction.UP) {
				Request request = currentJobs.pollLast();
				if (request.getInternalRequest().getDestinationFloor() < currentRequest.getInternalRequest()
						.getDestinationFloor()) {
					currentJobs.add(request);
					currentJobs.add(currentRequest);
					return true;
				}
				currentJobs.add(request);

			}

			if (currentDirection == Direction.DOWN) {
				Request request = currentJobs.pollFirst();
				if (request.getInternalRequest().getDestinationFloor() > currentRequest.getInternalRequest()
						.getDestinationFloor()) {
					currentJobs.add(request);
					currentJobs.add(currentRequest);
					return true;
				}
				currentJobs.add(request);

			}

		}
		return false;

	}

	private void moveToSourceFloor(Integer sourceFloor) {
		int startfloor = currentFloor;
		if (startfloor < sourceFloor) {
			for (int i = startfloor; i <= sourceFloor; i++) {
				currentFloor = i;
				System.out.println("The elevator has reached " + currentFloor);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (startfloor > sourceFloor) {
			for (int i = startfloor; i >= sourceFloor; i--) {
				currentFloor = i;
				System.out.println("The elevator has reached " + currentFloor);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addPendingDownJobsToCurrentJobs() {
		if (!downPendingJobs.isEmpty()) {
			System.out.println("\nAdded pending down jobs to current jobs\n");
			currentJobs = downPendingJobs;
			currentDirection = Direction.DOWN;
		} else {
			currentState = State.IDLE;
			System.out.print("\nThe elevator is in Idle state");
		}

	}

	private void addPendingUpJobsToCurrentJobs() {
		if (!upPendingJobs.isEmpty()) {
			System.out.println("\nAdded pending up jobs to current jobs\n");

			currentJobs = upPendingJobs;
			currentDirection = Direction.UP;
		} else {
			currentState = State.IDLE;
			System.out.print("\nThe elevator is in Idle state");

		}

	}

	public void addJob(Request request) {
		if (currentState == State.IDLE) {
			// check if the elevator is already on the floor on which the user
			// is if yes then we can directly process the destination floor
			if (currentFloor == request.getExternalRequest().getSourceFloor()) {
				System.out.println("Added current queue job -- lift state is - " + currentState + " location is - "
						+ currentFloor + " to move to floor - " + request.getInternalRequest().getDestinationFloor());
			}
			// check if the elevator is already on the floor on which the user
			// is if no then elevator first needs to move to source floor
			else {
				System.out.println("Added current queue job -- lift state is - " + currentState + " location is - "
						+ currentFloor + " to move to floor - " + request.getExternalRequest().getSourceFloor());
			}
			currentState = State.MOVING;
			currentDirection = request.getExternalRequest().getDirectionToGo();
			currentJobs.add(request);
		} else if (currentState == State.MOVING) {
			// only add to current que if the elevator is going in the same direction as the
			// request and is going to hit the pickup floor after the current floor.
			if (request.getExternalRequest().getDirectionToGo() != currentDirection) {
				addtoPendingJobs(request);
			} else if (request.getExternalRequest().getDirectionToGo() == currentDirection) {
				if (currentDirection == Direction.UP && request.getExternalRequest().getSourceFloor() <= currentFloor) {
					addtoPendingJobs(request);
				} else if (currentDirection == Direction.DOWN
						&& request.getInternalRequest().getDestinationFloor() > currentFloor) {
					addtoPendingJobs(request);
				} else {
					currentJobs.add(request);
				}

			}

		}

	}

	public void addtoPendingJobs(Request request) {
		if (request.getExternalRequest().getDirectionToGo() == Direction.UP) {
			System.out.println("Request added to pending up jobs\n");
			upPendingJobs.add(request);
		} else {
			System.out.println("Request added to pending down jobs\n");
			downPendingJobs.add(request);
		}
	}

}

enum State {

	MOVING, STOPPED, IDLE

}

enum Direction {

	UP, DOWN

}

class Request implements Comparable<Request> {
	private InternalRequest internalRequest;
	private ExternalRequest externalRequest;
	private boolean insideElevator = false;

	public Request(InternalRequest internalRequest, ExternalRequest externalRequest) {
		this.internalRequest = internalRequest;
		this.externalRequest = externalRequest;

	}

	public InternalRequest getInternalRequest() {
		return internalRequest;
	}

	public void setInternalRequest(InternalRequest internalRequest) {
		this.internalRequest = internalRequest;
	}

	public ExternalRequest getExternalRequest() {
		return externalRequest;
	}

	public void setExternalRequest(ExternalRequest externalRequest) {
		this.externalRequest = externalRequest;
	}

	public void setInsideElevator() {
		this.insideElevator = true;
	}

	public boolean getInsideElevator() {
		return insideElevator;
	}

	@Override
	public int compareTo(Request req) {
		if (this.getExternalRequest().getSourceFloor() == req.getExternalRequest().getSourceFloor())
			return 0;
		else if (this.getExternalRequest().getSourceFloor() > req.getExternalRequest().getSourceFloor())
			return 1;
		else
			return -1;
	}

}

class ProcessJobWorker implements Runnable {

	private Elevator elevator;

	ProcessJobWorker(Elevator elevator) {
		this.elevator = elevator;
	}

	@Override
	public void run() {
		/**
		 * start the elevator
		 */
		elevator.startElevator();
	}

}

class AddJobWorker implements Runnable {

	private Elevator elevator;
	private Request request;

	AddJobWorker(Elevator elevator, Request request) {
		this.elevator = elevator;
		this.request = request;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		elevator.addJob(request);
	}

}

class ExternalRequest {

	private Direction directionToGo;
	private int sourceFloor;

	public ExternalRequest(Direction directionToGo, int sourceFloor) {
		this.directionToGo = directionToGo;
		this.sourceFloor = sourceFloor;
	}

	public Direction getDirectionToGo() {
		return directionToGo;
	}

	public void setDirectionToGo(Direction directionToGo) {
		this.directionToGo = directionToGo;
	}

	public int getSourceFloor() {
		return sourceFloor;
	}

	public void setSourceFloor(int sourceFloor) {
		this.sourceFloor = sourceFloor;
	}

	@Override
	public String toString() {
		return " The Elevator has been requested on floor - " + sourceFloor + " and the person wants go in the - "
				+ directionToGo;
	}

}

class InternalRequest {
	private int destinationFloor;

	public InternalRequest(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}

	public int getDestinationFloor() {
		return destinationFloor;
	}

	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}

	@Override
	public String toString() {
		return "The destinationFloor is - " + destinationFloor;
	}

}

public class TestElevator {

	public static void main(String args[]) throws InterruptedException {

		Elevator elevator = new Elevator();

		/**
		 * Thread for starting the elevator
		 */
		ProcessJobWorker processJobWorker = new ProcessJobWorker(elevator);
		Thread t2 = new Thread(processJobWorker);
		t2.start();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Test 1: Test for generic up command
		ExternalRequest er = new ExternalRequest(Direction.UP, 0);
		InternalRequest ir = new InternalRequest(4);
		Request request1 = new Request(ir, er);
		new Thread(new AddJobWorker(elevator, request1)).start();

		// Test 2: Combind with Test 1 -- Test for up multiple up commmand and pickup
		// in motion if pickup floor is less then the current floor

		// ExternalRequest er1 = new ExternalRequest(Direction.UP, 1);
		// InternalRequest ir1 = new InternalRequest(4);
		// Request request2 = new Request(ir1, er1);
		// Thread.sleep(3000);
		// new Thread(new AddJobWorker(elevator, request2)).start();

		/*
		 * //Test 3: Combind with Test 1 -- Test for up multiple up commmand and a
		 * higher destination floor then the original command is added
		 * ExternalRequest er2 = new ExternalRequest(Direction.UP, 4);
		 * InternalRequest ir2 = new InternalRequest(6);
		 * Request request3 = new Request(ir2, er2);
		 * Thread.sleep(7000);
		 * new Thread(new AddJobWorker(elevator, request3)).start();
		 */

		// Test 4: Combind with Test 1 -- Test for up multiple up commmands that where
		// multiple passengers get on a single floor
		ExternalRequest er1 = new ExternalRequest(Direction.UP, 1);
		InternalRequest ir1 = new InternalRequest(4);
		Request request2 = new Request(ir1, er1);
		Thread.sleep(3000);
		new Thread(new AddJobWorker(elevator, request2)).start();

		// Test 1: Test for generic down command
		/*
		 * ExternalRequest er = new ExternalRequest(Direction.DOWN, 5);
		 * InternalRequest ir = new InternalRequest(0);
		 * Request request1 = new Request(ir, er);
		 * new Thread(new AddJobWorker(elevator, request1)).start();
		 */

		// Test 2: Combind with Test 1 -- Test for up multiple up commmand and pickup in
		// motion if pickup floor is less then the current floor
		/*
		 * ExternalRequest er1 = new ExternalRequest(Direction.DOWN, 2);
		 * InternalRequest ir1 = new InternalRequest(0);
		 * Request request2 = new Request(ir1, er1);
		 * Thread.sleep(3000);
		 * new Thread(new AddJobWorker(elevator, request2)).start();
		 */
		/*
		 * //Test 3: Combind with Test 1 -- Test for up multiple up commmand and a
		 * higher destination floor then the original command is added
		 * ExternalRequest er2 = new ExternalRequest(Direction.UP, 4);
		 * InternalRequest ir2 = new InternalRequest(6);
		 * Request request3 = new Request(ir2, er2);
		 * Thread.sleep(7000);
		 * new Thread(new AddJobWorker(elevator, request3)).start();
		 */

		// new Thread(new AddJobWorker(elevator, new Request(new InternalRequest(2), new
		// ExternalRequest(Direction.DOWN, 3)))).start();
		// new Thread(new AddJobWorker(elevator, new Request(new InternalRequest(1), new
		// ExternalRequest(Direction.DOWN, 4)))).start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
