package dnal.dio;

import java.util.List;

import org.mef.dnal.validation.ValidationBase;

import dnal.EarlyDNALTests.MutatorBase;

public class PositionMutator extends MutatorBase<PositionDIO> {
	private int x;
	private int y;

	public PositionMutator() {
	}
	public PositionMutator(PositionDIO obj) {
		x = obj.getX();
		y = obj.getY();
	}

	@Override
	protected void addValidators(List<ValidationBase> validators) {
	}

	@Override
	protected PositionDIO createObject() {
		PositionDIO obj = new PositionDIO(x, y);
		return obj;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
}
