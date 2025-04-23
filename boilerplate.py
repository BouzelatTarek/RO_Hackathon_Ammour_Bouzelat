class TapInstance:

    def __init__(self, file, epsilon_t, epsilon_d):
        n, I, T, D = load_instance(file)
        self.file = file
        self.size = n
        self.interest = I
        self.time = T
        self.dist = D
        self.epsilon_t = epsilon_t
        self.epsilon_d = epsilon_d

    def __repr__(self) -> str:
        return "TAP Instance of Size " + str(self.size) + " from '" + self.file + "'"

    def check_solution(self, sol):
        return self.sol_len(sol) <= self.epsilon_d and self.sol_time(sol) <= self.epsilon_t

    def sol_len(self, sol):
        d = 0
        for i in range(len(sol-1)):
            d += self.dist[sol[i]][sol[i+1]]
        return d

    def sol_time(self, sol):
        return sum(map(lambda i: self.time[i], sol))

    def sol_interest(self, sol):
        return sum(map(lambda i: self.interest[i], sol))


def load_instance(file):
    with open(file) as f:
        n = int(f.readline())
        I = []
        for e in f.readline().split(" "):
            I.append(float(e))
        T = []
        for e in f.readline().split(" "):
            T.append(float(e))
        D = []
        for i in range(n):
            d = []
            for e in f.readline().split(" "):
                d.append(float(e))
            D.append(d)
    return n, I, T, D



