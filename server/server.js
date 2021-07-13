const SocketServer = require('websocket').server
const http = require('http')
const server = http.createServer((req, res) => {})
server.listen(443, () => {
    console.log("Listening on port 443...")
})
wsServer = new SocketServer({httpServer:server})
var connections = []
var ans = ""
var is_full = 0
var id_list = []
var word_list = ['PL', '김철환', '박민경', '사과', '바나나', '수박', '고양이', '표장욱', '박윤지', '예수님', '피글렛', '농구공', '지갑', '컴퓨터', '티셔츠', '코로나', '시계', '공룡', '닭', '초콜릿', '껌', '안경', '휴지', '물병', 
'배트맨', '메로나', '모자', '계란', '알약', '헬창', '과로사', '순당무', '우유', '딸기', '나락송', '선풍기', '줌', '콜라', '테슬라', '800층', '동전', '도지', '구글', '고니', 
'서버', '클라', '프론트', '백엔드', '벚꽃', '육회', '비빔밥', '리엑트', '카톡', '마우스', '카드', '야식', '야근', '출근', '워라벨', '붕괴', '인생', '무상', '카르텔', '나무', '주식']
const timer = ms => new Promise(res => setTimeout(res, ms))
async function turn(connections) {
    console.log(connections.length)
    var k = 0
    for (let j = 0; j < 2; j++){
        for (let i = 0; i < connections.length; i += 2){
            var myturn = {"quiz": "true", "word": word_list[k]}
            var myturnstring = JSON.stringify(myturn)
            var notmyturn = {"quiz": "true", "word": ""}
            var notmyturnstring = JSON.stringify(notmyturn)
            var hiddenans = {"quiz": "false", "word": word_list[k]}
            var hiddenansstring = JSON.stringify(hiddenans)
            connections[i].send(myturnstring)
            connections[i+1].send(myturnstring)
            connections.forEach(element => {
                if (element != connections[i] && element != connections[i+1])
                element.send(notmyturnstring)
            })
            var t =0
            var timerStart = {"timer": "true"}
            var timerStartstring = JSON.stringify(timerStart)
            connections.forEach(element => {
                element.send(timerStartstring)
            })
            while(t < 16){
                await timer(1000)
                if (ans.localeCompare(word_list[k]) == 0) {
                    var getPoints = {"getPoint": "1", "points" : t}
                    var getPointsString = JSON.stringify(getPoints)
                    connections.forEach(element => {
                        if (element != connections[i] && element != connections[i+1])
                            element.send(getPointsString)
                    })
                    break;
                }
                t++
            }
            k++
        }
    }
    var finish = {"finish": "true"}
    var finsihString = JSON.stringify(finish)
    connections.forEach(element => {
        element.send(finsihString)
    })

}
wsServer.on('request', (req) => {
    const connection = req.accept()
    console.log("new connection")
    connections.push(connection)
    connection.on("message", (mes) => {
        console.log(mes);
        var jsonObject = JSON.parse(mes.utf8Data)
        if (Object.keys(jsonObject).includes("message")){
            ans = jsonObject.message
        }

        if (is_full == 0){
            if(Object.keys(jsonObject).includes("complete")){
                is_full = 1
                connections = []
                setTimeout(() => {
                    turn(connections)
                }, 3500)
            }
        }

        connections.forEach(element => {
            if (element != connection)
            element.sendUTF(mes.utf8Data)
        })
    })
    connection.on("canvas", (mes) => {
        console.log(mes);
        connections.forEach(element => {
            if (element != connection)
            element.sendUTF(mes)
        })
    })

    if (connections.length == 2){
        var gameStart = {"full": "true"}
        var gameStartstring = JSON.stringify(gameStart)
        connections.forEach(element => {
            element.send(gameStartstring)
            //setTimeout(() => console.log("after"), 1000)
        })
    }

 


    connection.on('close', (resCode, des) => {
        connections = []
        console.log('connection closed')
        //connection.splice(connections.indexOf(connection), 1)
    })
})
