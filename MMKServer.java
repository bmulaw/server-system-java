
public class MMKServer extends SimpleServer {
    private int failure = 0;
    private Integer K;

    public MMKServer(Timeline timeline, Double servTime, Integer K) {
        super(timeline, servTime);
        this.K = K;
    }

    @Override
    void receiveRequest(Event evt) {
        super.receiveRequest(evt);
        // check if we surprised K limit
        int n = super.theQueue.size();
        if (n >= this.K) {
            Request req = super.theQueue.removeLast();
            System.out.println(req + " DROP" + (super.name != null ? " " + super.name : "") + ": " + evt.getTimestamp());
            this.failure++;
        }
    }

    @Override
    void printStats(Double time) {
        if (this.name == null) {
            System.out.println("UTIL: " + busyTime/time);
            System.out.println("QLEN: " + cumulQ/snapCount);
            System.out.println("TRESP: " + cumulTq/servedReqs);
        } else {
            System.out.println();
            System.out.println(this.name + " UTIL: " + busyTime/time);
            System.out.println(this.name + " QLEN: " + cumulQ/snapCount);
            System.out.println(this.name + " TRESP: " + cumulTq/servedReqs);
            System.out.println(this.name + " DROPPED: "+ failure);
        }
    }
    // need avg
    public Double getAverage() {
//        System.out.println("this is MMK cumulq: " + cumulQ);
        return (cumulQ/snapCount);
    }
}
