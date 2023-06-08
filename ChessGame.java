import java.util.Scanner;

public class ChessGame {
    public ChessBoard Chessboard;
    boolean isWhiteTurn=true;
    int WhiteScore;
    int BlackScore;
    int turn = 50;// 총 턴
    int fromX, fromY, toX, toY;
    int escapeFlag = 999;
    //999: 기본상태
    //888: 기물을 다시 선택하는 경우(보드 출력 안함, isTurnleft())
    //-1: 긴급종료
    //0: 기물을 다시 선택하는 경우
    boolean castlingFlag = false;
    int apFlag = -1;
    int WhiteKingX, WhiteKingY, BlackKingX, BlackKingY;// 킹의 위치
    //isKingDie()에서 위치 동기화해줌.
    Scanner scan = new Scanner(System.in);
    String printMessage = "Game Start!!";

    public ChessGame() {

    }

    public void StartGame() {

        initBoard();
        // 체스 게임 시작
        // 킹이 죽거나 무승부(턴수 제한이 아니면)
        while (!isKingdie() && !isTurnsleft()) {
            // 보드 출력 다음 조건문은 기물을 다시선택하는 경우 보드를 출력하지 않기 위함.
            if(escapeFlag !=888){//기본 경우
                Chessboard.printBoard();
                printMessage+=isCheck();//체크확인 문자열 추가
                System.out.println(printMessage);
            }else{//888인 경우
                escapeFlag =999;//flag 초기화
            }
            // 보드 밑에 출력문 출력.
            // printMessage 함수에서 전역변수로 변경.
            // 기존의 printMessage()의 기능은 Chessboard.Move()에서 문자열 리턴.
            // 초기값은 Game Start!!
            // 사용자 입력 받기
            inputFrom(isWhiteTurn);
            if (escapeFlag == -1) {// 긴급종료
                break;
            }
            if(castlingFlag) {
                // 캐슬링의 경우 toX, toY 입력받지 않음. 따라서 Chessboard.Move()를 사용하지 않는다.
                // 그래서 printMessage를 직접 설정해줌.
                // 좌표 설정은 this.canCastle()에서 해줌.
                castlingFlag = false;//flag 초기화
                if(isWhiteTurn) {
                    printMessage = "White Castling\n";
                }else {
                    printMessage = "Black Castling\n";
                }

            }else{// 캐슬링이 아닌 경우
                inputTo(isWhiteTurn);
                if (escapeFlag == 0) {// 기물을 다시 선택하는 경우
                    escapeFlag =888;//flag 변경(보드 출력 안함, isTurnleft()
                    if(isWhiteTurn) {
                        System.out.println("White Re-selecting");
                    }else {
                        System.out.println("Black Re-selecting");
                    }
                    continue;
                } else if (escapeFlag == -1) {// 긴급종료
                    break;
                }
                // 이동
                printMessage = Chessboard.Move(fromX, fromY, toX, toY);
            }

            if (apFlag > -1){
                if (apFlag == 0){
                    Chessboard.apX = -1;
                    Chessboard.apY = -1;
                }
                apFlag--;
            }

            // 턴 바꾸기
            if(isWhiteTurn==true) {
                isWhiteTurn= false;
            }else {
                isWhiteTurn=true;
            }
            turn--;
        }
        printEnding();
    }

    // 재하
    public int pieceColor(int x, int y) {
        // 해당 좌표의 기물의 색이 무엇인지 출력.
        // 흑이면 -1, 백이면 +1, 비었으면 0 리턴
        // piece를 사용하여 판별

        ChessPiece piece = this.Chessboard.board[x][y];
        if(piece==null) {
            return 0;
        }
        else if(piece.isWhite == true) {
            return 1;
        }
        else{
            return -1;
        }
    }

    // 재하
    private void inputFrom(boolean isWhiteTurn) {
        // 선택할 말의 위치를 선택하시오: "A8"에서 문자열 추출 후 toX:1, toY:8 대입
        // 만일 잘못된 위치를 선택 시 오류 메세지(위치가 잘못되었습니다. 다시 입력하세요.) 출력 후 재입력 받음
        // 결국 이 함수에 목적은 움직일 말이 있는 정상 위치를 입력받는 것임.
        // 정상 값을 입력할때까지 이 함수 안에서 다시 입력받을 것
        // pieceColor() 사용하기

        while (true) {
            if(isWhiteTurn)
                System.out.print("Select White piece: ");
            else
                System.out.print("Select Black piece: ");
            //  fromX, fromY 입력받기
            String inputstr = scan.nextLine();
            // 예외처리
            if(inputstr.equals("quit")) {
                this.escapeFlag = -1;
                break;
            } else if(inputstr.length() == 2 && inputstr.charAt(0) >= 'A' && inputstr.charAt(0) <= 'H'
                    && inputstr.charAt(1) >= '1' && inputstr.charAt(1) <= '8') {
                // 정상입력
                this.fromX = inputstr.charAt(0) - 'A' + 1;
                this.fromY = inputstr.charAt(1) - '0';
                //디버깅용
                if (pieceColor(fromX, fromY) == 0) {//비어있는 경우
                    System.out.println("There is no piece");
                } else if (isWhiteTurn == true) {
                    // 백 차례일 때 백의 말을 선택한 경우
                    if (pieceColor(fromX, fromY) == 1) {
                        System.out.println("White Selected " + Chessboard.board[fromX][fromY].getFullname());
                        break;
                    }
                    // 백 차례일 때 흑의 말을 선택하거나 선택한 좌표가 비어있는 경우
                    else {
                        System.out.println("Selected opponent's piece");
                        continue;
                    }
                } else {
                    // 흑 차례일 때 백의 말을 선택한 경우
                    if (pieceColor(fromX, fromY) == -1) {
                        System.out.println("Black Selected " + Chessboard.board[fromX][fromY].getFullname());
                        break;
                    }
                    // 흑 차례일 때 백의 말을 선택하거나 선택한 좌표가 비어있는 경우
                    else if (pieceColor(fromX, fromY) == 1 || pieceColor(fromX, fromY) == 0) {
                        System.out.println("Selected opponent's piece");
                        continue;
                    }
                }
            } else if(inputstr.length()==2&&(inputstr.charAt(0)=='Q'||inputstr.charAt(0)=='K')&&inputstr.charAt(1)=='C'){
                //퀸,킹 캐슬 KC QC
                //캐슬링이 안되는 경우 'Failed to castle' 출력 후 재입력 받기.(밑에 input error) 참고할 것.
                if(canCastle(inputstr.charAt(0))==false){//캐슬링이 안되는 경우
                    System.out.println("Failed to castle");
                    continue;
                } else{//캐슬링이 되는 경우
                    this.castlingFlag=true;
                    break;
                }
            } else {
                //재입력
                System.out.println("input error");
                continue;
            }
            // str에 "'A~H"+'1~8'문자열 입력 ex) A8"을 입력받아서 fromX:1, fromY:8 대입 (문자열 처리)
            // 예외처리: 잘못된 문자열 입력 시 오류 메세지 출력 후 재입력 받음
            // str에 fromX, fromY 추출
            // 예외처리: 잘못된 위치 입력 시 오류 메세지 출력 후 재입력 받음
            // 예외처리: 비어있는 위치 입력 시 오류 메세지 출력 후 재입력 받음
            // 예외처리: 흑의 차례에 백의 말을 선택한 경우 오류 메세지 출력 후 재입력 받음
            // 예외처리: 백의 차례에 흑의 말을 선택한 경우 오류 메세지 출력 후 재입력 받음

        }
    }

    // 찬규
    private void inputTo(boolean isWhiteTurn) {
        // 선택할 말의 위치를 선택하시오: "A8"에서 문자열 추출 후 toX:1, toY:8 대입
        // 만일 잘못된 위치를 선택 시 오류 메세지 출력 후 재입력 받음
        while (true) {
            System.out.print("Move to:");
            // toX, toY 입력받기
            String inputstr = scan.nextLine();
            // 예외처리: 잘못된 문자열 입력 시 오류 메세지 출력 후 재입력 받음
            if (inputstr.equals("quit")) {// 긴급종료
                this.escapeFlag = -1;
                break;
            } else if (inputstr.equals("back")) { // 기물을 다시 선택하는 경우
                this.escapeFlag = 0;
                break;
            } else if (inputstr.length() == 2 && inputstr.charAt(0) >= 'A' && inputstr.charAt(0) <= 'H'
                    && inputstr.charAt(1) >= '1' && inputstr.charAt(1) <= '8') {
                // 정상입력
                this.toX = inputstr.charAt(0) - 'A' + 1;
                this.toY = inputstr.charAt(1) - '0';
            } else {
                // 재입력
                System.out.println("input error");
                continue;
            }
            String str = this.Chessboard.board[fromX][fromY].canMove(fromX, fromY, toX, toY);
            if (str.equals("move")) {// 정상이동
                break;
            } else if (str.equals("eat")) {// 정상이동
                break;
            } else {
                // 재입력
                System.out.println(str);
                continue;
            }
        }
    }

    // 치수
    private void printEnding() {
        // ex) White Win!!
        if (isKingdie()) { //king이 죽어서 끝난 경우
            if (isWhiteTurn) { //black 승
                System.out.println("White King dead, Black Win !!");
            } else { //white 승
                System.out.println("Black King dead, White Win!!");
            }
        } else if (isTurnsleft()) { // 턴이 끝나서 종료된 경우
            if (WhiteScore > BlackScore) {
                System.out.println("White Score : "+WhiteScore+"\nBlack Score : "+BlackScore+"\nWhite Win !!");
            } else if (WhiteScore < BlackScore) {
                System.out.println("White Score : "+WhiteScore+"\nBlack Score : "+BlackScore+"\nBlack Win !!");
            } else {
                System.out.println("White Score : "+WhiteScore+"\nBlack Score : "+BlackScore+"\nDraw !!");
            }

        } else {
            System.out.println("Exiting the game");
        }

    }

    // 주혁
    private void initBoard() {
        // 시작 방법?
        // 체스 게임을 시작으로 체스에서 체스 보드를 불러와야 하는데 체스 보드에서는 현재의 체스판을 인자로 받아와야함
        // 이때 ChessBoard(ChessGame game)에서 game에 자기 자신을 넣을 수 없으니 충돌이 발생
        // Chessboard를 초기화 시킬 수 없어서 시작이 안됨

        this.Chessboard = new ChessBoard(this);

        for(int i=1;i<9;i++){
            Chessboard.board[i][2] = new Pawn(true, this.Chessboard);
        }
        for(int i=1;i<9;i++){
            Chessboard.board[i][7] = new Pawn(false, this.Chessboard);
        }

        Chessboard.board[1][1] = new Rook(true, this.Chessboard);
        Chessboard.board[8][1] = new Rook(true, this.Chessboard);
        Chessboard.board[1][8] = new Rook(false, this.Chessboard);
        Chessboard.board[8][8] = new Rook(false, this.Chessboard);
        Chessboard.board[2][1] = new Knight(true, this.Chessboard);
        Chessboard.board[7][1] = new Knight(true, this.Chessboard);
        Chessboard.board[2][8] = new Knight(false, this.Chessboard);
        Chessboard.board[7][8] = new Knight(false, this.Chessboard);
        Chessboard.board[3][1] = new Bishop(true, this.Chessboard);
        Chessboard.board[6][1] = new Bishop(true, this.Chessboard);
        Chessboard.board[3][8] = new Bishop(false, this.Chessboard);
        Chessboard.board[6][8] = new Bishop(false, this.Chessboard);
        Chessboard.board[4][1] = new Queen(true, this.Chessboard);
        Chessboard.board[5][1] = new King(true, this.Chessboard);
        Chessboard.board[4][8] = new Queen(false, this.Chessboard);
        Chessboard.board[5][8] = new King(false, this.Chessboard);
    }


    // 경식
    public boolean isKingdie() {
            int k = 0;
            for (int i = 1; i < 9; i++) {
                for(int j = 1; j < 9; j++){
                    if(this.Chessboard.board[i][j] == null){
                        continue;
                    }
                    if (Chessboard.board[i][j] instanceof King) {
                        if(Chessboard.board[i][j].isWhite){//킹들 위치 동기화
                            WhiteKingX= i;
                            WhiteKingY= j;
                        }else{
                            BlackKingX= i;
                            BlackKingY= j;
                        }
                        k++;
                    }
                }
            }
            if(k == 2){
                return false;
            }
            else{
                return true;
            }
        }
        // 킹이 죽었는지 확인
        // 킹이 죽었으면 true, 아니면 false
        // 체스판에서 흑과 백의 킹이 죽었는지 확인
        // 체스판을 모두 돌면서 킹이 두개 존재하는지 확인하면 될 듯


    // 치수
    public boolean isTurnsleft() { //남은 턴수 계산, 0이되면 true 반환
        if(turn>0){
            return false;
        }
        else{
            return true;
        }
    }

    public String isCheck(){
        //White King Checked
        //Black King Checked 리턴
        //만약 동시 체크면 둘다 리턴(White King Check\nBlack King Check) 리턴
        //체크가 아니면 빈 문자열 리턴
        //각 체스 기물의 canCheck() 함수를 호출해서 체크인지 확인

        //******** 어떤 기물에 의해서 Checked 되었는지 표기함. ********* 
        //개행문자 필요해서 str에 기본적으로 개행문자 넣어놓음.
        String str="\n";
        for(int i=1;i<9;i++){
            for(int j=1;j<9;j++){
                if(Chessboard.board[i][j]!=null){
                    if(Chessboard.board[i][j].isWhite){
                        if(Chessboard.board[i][j].canCheck(i, j, BlackKingX, BlackKingY)){
                            str+="Black King Checked by White "+Chessboard.board[i][j].getFullname()+"\n";
                        }
                    }
                    else{
                        if(Chessboard.board[i][j].canCheck(i, j, WhiteKingX, WhiteKingY)){
                            str+="White King Checked by Black "+Chessboard.board[i][j].getFullname()+"\n";
                        }
                    }
                }
            }
        }
        
        return str;
    }
    public boolean canCastle(char KorQ){
        //캐슬링 가능 여부 리턴
        //캐슬링 가능하면 true, 아니면 false
        //캐슬링이 가능하다면, ChessBoard의 기물 위치까지 바꾸기(Move()를 사용하지 않을 것이기 떄문에.)
        //즉 ChessBoard의 기물 위치를 바꾸고, King, Rook의 isFisrtMove를 false로 바꾸기
        //King, Rook의 isFisrtMove 활용하기.
        if(isWhiteTurn) {//백 차례일 때
            if (KorQ == 'Q') { //QC경우
                //킹과 룩 사이 기물들이 없는 지 확인
                for(int i=2;i<5;i++){
                    if(Chessboard.board[i][1]!=null){
                        //기물이 있다면 false 리턴
                        return false;
                    }
                }
                if((Chessboard.board[1][1] instanceof Rook)&&(Chessboard.board[5][1] instanceof King)){ //A1과 E1이 룩과 킹 인지 확인
                    Rook r=(Rook) Chessboard.board[1][1];
                    King k=(King) Chessboard.board[5][1];
                    if(r.isFirstMove&&k.isFirstMove){
                        //룩과 킹이 모두 움직인 적이 없다면isfirstmove false로
                        r.isFirstMove=false;
                        k.isFirstMove=false;
                        //룩과 킹 이동
                        Chessboard.board[4][1]=r;
                        Chessboard.board[3][1]=k;
                        //기존 자리 null로 변경
                        Chessboard.board[1][1]=null;
                        Chessboard.board[5][1]=null;
                        //true 리턴
                        return true;
                    }
                    
                }

            } else{//KC
                 //킹과 룩 사이 기물들이 없는 지 확인
                 for(int i=6;i<8;i++){
                    if(Chessboard.board[i][1]!=null){
                        //기물이 있다면 false 리턴
                        return false;
                    }
                }
                if((Chessboard.board[8][1] instanceof Rook)&&(Chessboard.board[5][1] instanceof King)){ //H1과 E1이 룩과 킹 인지 확인
                    Rook r=(Rook) Chessboard.board[8][1];
                    King k=(King) Chessboard.board[5][1];
                    if(r.isFirstMove&&k.isFirstMove){
                        //룩과 킹이 모두 움직인 적이 없다면isfirstmove false로
                        r.isFirstMove=false;
                        k.isFirstMove=false;
                        //룩과 킹 이동
                        Chessboard.board[6][1]=r;
                        Chessboard.board[7][1]=k;
                        //기존 자리 null로 변경
                        Chessboard.board[8][1]=null;
                        Chessboard.board[5][1]=null;
                        //true 리턴
                        return true;
                    }
                    
                }

            }
        }else {//흑 차례일 때
            if (KorQ == 'Q') {//퀸 캐슬
                //킹과 룩 사이 기물들이 없는 지 확인
                for(int i=2;i<5;i++){
                    if(Chessboard.board[i][8]!=null){
                        //기물이 있다면 false 리턴
                        return false;
                    }
                }
                if((Chessboard.board[1][8] instanceof Rook)&&(Chessboard.board[5][8] instanceof King)){ //A1과 E1이 룩과 킹 인지 확인
                    Rook r=(Rook) Chessboard.board[1][8];
                    King k=(King) Chessboard.board[5][8];
                    if(r.isFirstMove&&k.isFirstMove){
                        //룩과 킹이 모두 움직인 적이 없다면isfirstmove false로
                        r.isFirstMove=false;
                        k.isFirstMove=false;
                        //룩과 킹 이동
                        Chessboard.board[4][8]=r;
                        Chessboard.board[3][8]=k;
                        //기존 자리 null로 변경
                        Chessboard.board[1][8]=null;
                        Chessboard.board[5][8]=null;
                        //true 리턴
                        return true;
                    }
                    
                }
            }else{//킹 캐슬
                //킹과 룩 사이 기물들이 없는 지 확인
                for(int i=6;i<8;i++){
                    if(Chessboard.board[i][8]!=null){
                        //기물이 있다면 false 리턴
                        return false;
                    }
                }
                if((Chessboard.board[8][8] instanceof Rook)&&(Chessboard.board[5][8] instanceof King)){ //H1과 E1이 룩과 킹 인지 확인
                    Rook r=(Rook) Chessboard.board[8][8];
                    King k=(King) Chessboard.board[5][8];
                    if(r.isFirstMove&&k.isFirstMove){
                        //룩과 킹이 모두 움직인 적이 없다면isfirstmove false로
                        r.isFirstMove=false;
                        k.isFirstMove=false;
                        //룩과 킹 이동
                        Chessboard.board[6][8]=r;
                        Chessboard.board[7][8]=k;
                        //기존 자리 null로 변경
                        Chessboard.board[8][8]=null;
                        Chessboard.board[5][8]=null;
                        //true 리턴
                        return true;
                    }
                    
                }
            }
        }
        return false;
    }
}
