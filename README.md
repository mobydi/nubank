# Job Queues task from nubank

In this exercise you're tasked with developing a simplified version of a job queue. The important entities in this
domain are *jobs* and *agents*.

A *job* is any task that needs to get done. It has a unique id, a type - which denotes the category of that job -, and
an urgency (boolean) flag - indicating whether that job has a high priority.

An *agent* is someone that performs a job. They also have a unique id and two disjoint skill sets: primary and
secondary. Skill sets are a simple list of job types that an agent is allowed to perform.

The core operation of a job queue is the dequeue function, which, given a pool of jobs to be done and agent's job
request, and a set of priority rules, returns the fittest job to be performed by that agent. Your first task is to
implement a dequeue function that abides to these rules:

- You can assume the list of jobs passed in is ordered by the time the they have entered the system.
- Jobs that arrived first should be assigned first, unless it has a "urgent" flag, in which case it has a higher
  priority.
- A job cannot be assigned to more than one agent at a time.
- An agent is not handed a job whose type is not among its skill sets.
- An agent only receives a job whose type is contained among its secondary skill set if no job from its primary
  skill set is available.

A job is considered to be done when the agent it was assigned to requests a new job.

Attached are two files: sample-input.json and sample-output.json. Your program should be able to take the
contents of the sample-input.json file via stdin and produce the contents of sample-output.json on stdout.

## Installation

You have to install java and lein

## Usage

### run program

```cat resources/sample-input.json | lein trampoline run```

### run tests

```lein test```

## Assumptions & Limitations

- There are three implementation of current task.
- Time tracker/spent time Sat 2h + Sun 3h + Wen 2h + Fri 3h + Sun 4h ~ 14 hours.
- Weak tests coverage, because I would fit into task time.

### core.clj

Reference implementation. Please look inside ```core.clj```.
I up to fix smth, if you find a bug. Please fill free to give feedback.

- There is limit, doesn't support adding new jobs after "job_request" execution
- Benefit. Optimised job list enumerations, for huge job list.
- Algorithm complexity ~ O(j) * O(r) * O(s), j - # of jobs, r - # of requests, s - # of skills. 

### simple.clj

Simplified solution, compare to ```core.clj```, but has double job list enumerations to remove a job from list. 

- Looks better, but has extra enumerations
- Algorithm complexity ~ O(2*j) * O(r) * O(s), j - # of jobs, r - # of requests, s - # of skills. It's not really good.
- A "degueue" function works according to requirement (it should returns the fittest job to be performed by that agent)

### /todo/state.clj

I would solve the task in common way, you can add extra jobs and agents after "job_request", it will work well. And this is an optimised algorithm with minimal scans and complexity.

- I solved this task using atom, sorry about that. Maybe there is a simple solution, I haven't got yet.
- I would optimise algorithm to minimize job dequeue func complexity, and eliminate linear job list scan(s).
- Algorithm job dequeue complexity is O("1")+O(1). The first O("1") get a skill from map and the second O(1) get the first item from the job list. (according to Rich Hickey, an "1" in quotes is that close enough to one to not matter)
- Full algorithm complexity ~ O("1")+O(1)+O(p)+O(s), p - # of primary_skills, s - # of secondary_skills, better that previous one.
