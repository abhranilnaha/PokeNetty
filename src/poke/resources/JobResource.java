/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.resources;

import poke.comm.App;
import poke.comm.App.*;
import poke.communication.CommunicationConnection;
import poke.server.Server;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;

import java.util.Map;

public class JobResource implements Resource {

	private ServerConf conf;

	@Override
	public Request process(Request request) {


		JobDesc data = request.getBody().getJobOp().getData();
		String nameSpace = data.getNameSpace();
		NameValueSet options = data.getOptions();
		conf = Server.sconf;

		System.out.println("Entered Job Resource");
		Request.Builder requestBuilder = Request.newBuilder().setHeader(request.getHeader());
		JobDesc.Builder jobDescBuilder = JobDesc.newBuilder();
		
		System.out.println("Received request from client for namespace: " + nameSpace);

		if (nameSpace.equals("sign_up")) {
			// sign up logic

			System.out.println("Entered Job Resource sign_up");
		} else if (nameSpace.equals("sign_in")) {
			// sign in logic
		}
		else if (nameSpace.equals("listcourses")) {

			System.out.println("Entered listcourses");
			jobDescBuilder.setOptions(NameValueSet.newBuilder()
					.setNodeType(options.getNodeType())
					.setName(options.getName())
					.setValue(options.getValue())
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Java")
							.setValue("Basic Java Tutorial"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Python")
							.setValue("Basic Python Learning"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("iOS")
							.setValue("Basic iOS Tutorial"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Android")
							.setValue("Basic Android Learning"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("AngularJS Tutorial")
							.setValue("AngularJS Framework Walkthrough")));
		}

		else if (nameSpace.equals("getdescription")) {
			jobDescBuilder.setOptions(NameValueSet.newBuilder()
					.setNodeType(options.getNodeType())
					.setName(options.getName())
					.setValue("Basic " + options.getValue() + " Tutorial"));
		}

		//channges

		else if (request.getBody().getJobOp().getData().getNameSpace().equals("intercluster"))
		{

			Request.Builder interReply = Request.newBuilder();

			// Header
			interReply.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), App.PokeStatus.SUCCESS, null));

			// payload
			App.Payload.Builder pb = App.Payload.newBuilder();
			App.JobStatus.Builder jsb = App.JobStatus.newBuilder();
			jsb.setJobId(request.getBody().getJobStatus().getJobId());
			jsb.setJobState(request.getBody().getJobStatus().getJobState());
			jsb.setStatus(App.PokeStatus.SUCCESS);

			App.JobDesc.Builder jobDesc = App.JobDesc.newBuilder();
			jobDesc.setNameSpace("listcourses");
			jobDesc.setOwnerId(1234);
			jobDesc.setJobId("100");
			jobDesc.setStatus(App.JobDesc.JobCode.JOBRECEIVED);

			System.out.println("Cluster Host and port!!!");

			int port, j = 0;
			String host, course, description, name;
			Request interclusterRequest = buildInterclusterRequest();

			for (Map.Entry<Integer, NodeDesc> cluster : conf.getAdjacentCluster().getAdjacentNodes().entrySet()) {
				port = cluster.getValue().getPort();
				host = cluster.getValue().getHost();
				name = cluster.getValue().getNodeName();
				System.out.println("Host :" + host + "Port : " + port);

				CommunicationConnection connection = new CommunicationConnection(host, port);
				Request res = connection.send(interclusterRequest);

				if (null != res) {
					int count = res.getBody().getJobStatus().getData(0).getOptions().getNodeCount();
					int i = 0;

					App.NameValueSet.Builder nv = App.NameValueSet.newBuilder();
					nv.setNodeType(App.NameValueSet.NodeType.VALUE);
					nv.setName("from cluster" + name);
					App.NameValueSet.Builder nvc = App.NameValueSet.newBuilder();

					for (int k = 0; k < count; k++) {
						course = res.getBody().getJobStatus().getData(0).getOptions().getNode(k).getName();
						description = res.getBody().getJobStatus().getData(0).getOptions().getNode(k).getValue();
						System.out.println("Course :" + course + "Description:" + description);

						nvc.setNodeType(App.NameValueSet.NodeType.VALUE);
						nvc.setName(course);
						nvc.setValue(description);
						nv.addNode(i++, nvc.build());
					}

					jobDesc.setOptions(nv.build());
					jsb.addData(j++, jobDesc.build());
				}

			}
			pb.setJobStatus(jsb.build());
			interReply.setBody(pb.build());
			System.out.println("Reply:::\n" + interReply.build());

			return interReply.build();

		}

		else if (request.getBody().getJobOp().getData().getNameSpace().equals("getInterclusterData"))
		{
			Request.Builder interReply = Request.newBuilder();

			System.out.println("----------------------->Entered");

			// Header
			interReply.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), App.PokeStatus.SUCCESS, null));

			// payload
			App.Payload.Builder pb = App.Payload.newBuilder();
			App.JobStatus.Builder jsb = App.JobStatus.newBuilder();
			jsb.setJobId(request.getBody().getJobStatus().getJobId());
			jsb.setJobState(request.getBody().getJobStatus().getJobState());
			jsb.setStatus(App.PokeStatus.SUCCESS);

			interReply.setBody(pb.build());
			NameValueSet.Builder nVSetBuilder = NameValueSet.newBuilder()
					.setNodeType(options.getNodeType())
					.setName(options.getName())
					.setValue(options.getValue())
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Java")
							.setValue("Basic Java Tutorial"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Python")
							.setValue("Basic Python Learning"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("iOS")
							.setValue("Basic iOS Tutorial"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("Android")
							.setValue("Basic Android Learning"))
					.addNode(NameValueSet.newBuilder()
							.setNodeType(NameValueSet.NodeType.VALUE)
							.setName("AngularJS Tutorial")
							.setValue("AngularJS Framework Walkthrough"));

			JobDesc.Builder jdesBuilder = JobDesc.newBuilder();
			jdesBuilder.setNameSpace(request.getBody().getJobOp().getData().getNameSpace());
			jdesBuilder.setOwnerId(request.getBody().getJobOp().getData().getOwnerId());
			jdesBuilder.setJobId(request.getBody().getJobOp().getJobId());
			jdesBuilder.setStatus(JobDesc.JobCode.JOBRECEIVED);
			jdesBuilder.setOptions(nVSetBuilder.build());

			jsb.setJobId(request.getBody().getJobOp().getJobId());
			jsb.setStatus(PokeStatus.SUCCESS);
			jsb.setJobState(JobDesc.JobCode.JOBRECEIVED);
			jsb.addData(jdesBuilder.build());
			jsb.addData(0, jdesBuilder.build());
			pb.setJobStatus(jsb.build());
			interReply.setBody(pb.build());
			System.out.println("Reply:::\n" + interReply.build());

			System.out.println("----------------------->"+interReply.build());
				return interReply.build();
		}



		jobDescBuilder.setNameSpace(nameSpace);
		jobDescBuilder.setOwnerId(data.getOwnerId());
		jobDescBuilder.setJobId(data.getJobId());
		jobDescBuilder.setStatus(data.getStatus());

		Payload.Builder payloadBuilder = Payload.newBuilder();

		JobStatus.Builder jobStatusBuilder = JobStatus.newBuilder();
		jobStatusBuilder.setJobId(data.getJobId());
		jobStatusBuilder.setJobState(data.getStatus());
		jobStatusBuilder.setStatus(PokeStatus.SUCCESS);
		jobStatusBuilder.addData(jobDescBuilder);

		payloadBuilder.setJobStatus(jobStatusBuilder);
		requestBuilder.setBody(payloadBuilder);

		System.out.println("Response is: " + request.getBody());
		
		return requestBuilder.build();
	}


	private Request buildInterclusterRequest() {
		Request.Builder forward = Request.newBuilder();

		App.Header.Builder header = App.Header.newBuilder();
		header.setOriginator(conf.getNodeId());
		header.setRoutingId(App.Header.Routing.JOBS);
		forward.setHeader(header.build());

		App.Payload.Builder forwardpb = App.Payload.newBuilder();

		App.JobStatus.Builder forwardjsb = App.JobStatus.newBuilder();
		forwardjsb.setJobId("5555");
		forwardjsb.setJobState(App.JobDesc.JobCode.JOBQUEUED);
		forwardjsb.setStatus(App.PokeStatus.SUCCESS);

		App.JobOperation.Builder forwardJobOp = App.JobOperation.newBuilder();
		forwardJobOp.setJobId("5555");
		forwardJobOp.setAction(App.JobOperation.JobAction.LISTJOBS);

		App.JobDesc.Builder forwardData = App.JobDesc.newBuilder();
		forwardData.setNameSpace("getInterclusterData");
		forwardData.setOwnerId(5555);
		forwardData.setJobId("5555");
		forwardData.setStatus(App.JobDesc.JobCode.JOBQUEUED);
		forwardJobOp.setData(forwardData.build());

		forwardpb.setJobOp(forwardJobOp.build());
		forwardpb.setJobStatus(forwardjsb.build());
		forward.setBody(forwardpb.build());
		return forward.build();
	}

}
