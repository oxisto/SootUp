package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import de.upb.swt.soot.callgraph.spark.builder.MethodNodeFactory;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ReferenceType;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CallTargetHandler {

  PointerAssignmentGraph pag;

  public CallTargetHandler(PointerAssignmentGraph pag) {
    this.pag = pag;
  }

  public void addCallTarget(Pair<MethodSignature, CalleeMethodSignature> edge) {
    MethodSignature source = edge.getKey();
    CalleeMethodSignature target = edge.getValue();
    CallGraphEdgeType edgeType = target.getEdgeType();
    if (!edgeType.passesParameters()) {
      return;
    }
    Optional<? extends SootMethod> sourceMethodOp =
        pag.getView()
            .getMethod(source); // MethodUtil.methodSignatureToMethod(pag.getView(), source);
    Optional<? extends SootMethod> targetMethodOp =
        pag.getView().getMethod(target.getMethodSignature());
    if (!sourceMethodOp.isPresent() || !targetMethodOp.isPresent()) {
      return;
    }
    SootMethod sourceMethod = sourceMethodOp.get();
    SootMethod targetMethod = targetMethodOp.get();

    IntraproceduralPointerAssignmentGraph srcIntraPag =
        IntraproceduralPointerAssignmentGraph.getInstance(pag, sourceMethod);
    IntraproceduralPointerAssignmentGraph tgtIntraPag =
        IntraproceduralPointerAssignmentGraph.getInstance(pag, targetMethod);

    if (edgeType.isExplicit()
        || edgeType == CallGraphEdgeType.THREAD
        || edgeType == CallGraphEdgeType.ASYNCTASK) {
      addCallTarget(srcIntraPag, tgtIntraPag, target.getSourceStmt(), edgeType);
    } else if (edgeType == CallGraphEdgeType.EXECUTOR) {
      // TODO: handle other types of edges
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.HANDLER) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.PRIVILEGED) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.FINALIZE) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.NEWINSTANCE) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.REFL_INVOKE) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else if (edgeType == CallGraphEdgeType.REFL_CLASS_NEWINSTANCE
        || edgeType == CallGraphEdgeType.REFL_CONSTR_NEWINSTANCE) {
      throw new NotImplementedException("EdgeType: " + edgeType.toString());
    } else {
      throw new RuntimeException("Unhandled edge " + edgeType);
    }
  }

  private void addCallTarget(
      IntraproceduralPointerAssignmentGraph sourceIntraPag,
      IntraproceduralPointerAssignmentGraph targetIntraPag,
      Stmt sourceStmt,
      CallGraphEdgeType edgeType) {
    MethodNodeFactory sourceNodeFactory = sourceIntraPag.getNodeFactory();
    MethodNodeFactory targetNodeFactory = targetIntraPag.getNodeFactory();
    AbstractInvokeExpr invokeExpr = sourceStmt.getInvokeExpr();
    boolean isVirtualCall = pag.getCallAssigns().containsKey(invokeExpr);
    handleCallTargetParams(
        sourceIntraPag, edgeType, sourceNodeFactory, targetNodeFactory, invokeExpr);

    handleCallTargetInstanceInvoke(
        sourceIntraPag, edgeType, sourceNodeFactory, targetNodeFactory, invokeExpr, isVirtualCall);

    handleCallTargetAssign(
        sourceIntraPag, sourceStmt, edgeType, sourceNodeFactory, targetNodeFactory, invokeExpr);
  }

  private void handleCallTargetParams(
      IntraproceduralPointerAssignmentGraph sourceIntraPag,
      CallGraphEdgeType edgeType,
      MethodNodeFactory sourceNodeFactory,
      MethodNodeFactory targetNodeFactory,
      AbstractInvokeExpr invokeExpr) {
    int numArgs = invokeExpr.getArgCount();
    for (int i = 0; i < numArgs; i++) {
      Value arg = invokeExpr.getArg(i);
      if (!(arg.getType() instanceof ReferenceType)) {
        continue;
      }
      if (arg instanceof NullConstant) {
        continue;
      }
      Node argNode = sourceNodeFactory.getNode(arg);
      // TODO: Parameterize argNode
      argNode = argNode.getReplacement();

      Node param = targetNodeFactory.caseParameter(i);
      // TODO: Parameterize param
      param = param.getReplacement();

      addInterProceduralCallTarget(sourceIntraPag, edgeType, invokeExpr, argNode, param);
    }
  }

  private void handleCallTargetInstanceInvoke(
      IntraproceduralPointerAssignmentGraph sourceIntraPag,
      CallGraphEdgeType edgeType,
      MethodNodeFactory sourceNodeFactory,
      MethodNodeFactory targetNodeFactory,
      AbstractInvokeExpr invokeExpr,
      boolean isVirtualCall) {
    if (invokeExpr instanceof AbstractInstanceInvokeExpr) {
      AbstractInstanceInvokeExpr instanceInvokeExpr = (AbstractInstanceInvokeExpr) invokeExpr;

      Node baseNode = sourceNodeFactory.getNode(instanceInvokeExpr.getBase());
      // TODO: Parameterize baseNode
      baseNode = baseNode.getReplacement();

      Node thisRef = targetNodeFactory.caseThis();
      // TODO: Parameterize thisRef
      thisRef = thisRef.getReplacement();
      addInterProceduralCallTarget(sourceIntraPag, edgeType, invokeExpr, baseNode, thisRef);
      if (isVirtualCall && !pag.getVirtualCallsToReceivers().containsKey(invokeExpr)) {
        pag.getVirtualCallsToReceivers().put(invokeExpr, baseNode);
      }
    }
  }

  private void handleCallTargetAssign(
      IntraproceduralPointerAssignmentGraph sourceIntraPag,
      Stmt sourceStmt,
      CallGraphEdgeType edgeType,
      MethodNodeFactory sourceNodeFactory,
      MethodNodeFactory targetNodeFactory,
      AbstractInvokeExpr invokeExpr) {
    if (sourceStmt instanceof JAssignStmt) {
      Value target = ((JAssignStmt) sourceStmt).getLeftOp();
      if (target.getType() instanceof ReferenceType && !(target instanceof NullConstant)) {
        Node targetNode = sourceNodeFactory.getNode(target);
        // TODO: parameterize targetNode
        targetNode = targetNode.getReplacement();

        Node returnNode = targetNodeFactory.caseReturn();
        // TODO: parameterize returnNode
        returnNode = returnNode.getReplacement();
        addInterProceduralCallTarget(sourceIntraPag, edgeType, invokeExpr, returnNode, targetNode);
      }
    }
  }

  private void addInterProceduralCallTarget(
      IntraproceduralPointerAssignmentGraph sourceIntraPag,
      CallGraphEdgeType edgeType,
      AbstractInvokeExpr invokeExpr,
      Node sourceNode,
      Node targetNode) {
    pag.addEdge(sourceNode, targetNode);
    Pair<Node, Node> pair = new ImmutablePair<>(sourceNode, targetNode);
    pag.getCallAssigns().put(invokeExpr, pair);
    pag.getCallToMethod().put(invokeExpr, sourceIntraPag.getMethod());
  }
}