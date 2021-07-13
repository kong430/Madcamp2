

# ContactsGalleryAndRain

> 장진혁, 박민경



### Overview

> 채팅 기능, 그림 공유가 가능한 어플리케이션이다. 턴재로 진행되는 게임이며, 상대방이 그린 그림을 맞추면 된다. 



### Environment

* Android Studio 4.2.1
* SDK Platforms: Android 11.0 - API Level: 30
* Language : JAVA, JavaScript
* Node.js : 16.4.2



### Login

> 3개의 탭 <Contacts, Gallery, Rain_notification> 을 가진다.

* TabLayout 과 ViewPager2 를 이용하여 구성하였다.

* MyViewAdapter 를 구현하여 viewPager 에 셋팅하였다. MyViewAdapter 에서 Fragment를 생성하였으며, tabLayout 의 onTabSelectedListner 를 구현하여 viewPager 의 출력 Fragment 와 현재 출력 화면을 맞추었다.

  


### Waiting

> 연락처를 보여주는 탭으로 연락처 검색, 추가, 전화를 할 수 있다.

![1625585381151](https://user-images.githubusercontent.com/56385667/124627610-aa74d980-deba-11eb-802e-fd276c625a36.gif)

- 연락처 읽기, 쓰기 권한은 스플래시 화면이 출력되는 동안 권한을 허용받는다.

- 상단에 있는 검색창을 통해 특정 연락처를 찾을 수 있다. 

  - Fragment1:ContactAdapter:ListFilter 를 구현하여 이용하였다. 필터링한 연락처 아이템을 Fragment1:ContactAdapter:getView() 를 통해 화면에 출력한다.

    

![1625585383552](https://user-images.githubusercontent.com/56385667/124627972-f9227380-deba-11eb-8f91-804423ec2251.gif)


* 오른쪽 하단에 있는 + 플로팅 버튼을 통해 새로운 연락처를 추가할 수 있다.
  
   * 이름과 연락처를 입력하는 PlusActivity 를 실행하여 핸드폰에 연락처를 추가한다.
   * 연락처 리스트를 스크롤하는 동안에는 플로팅 버튼이 사라진다.
   



![1625585379745](https://user-images.githubusercontent.com/56385667/124627503-9335ec00-deba-11eb-8cf1-413e3e8085fc.gif)



* 특정 연락처를 눌러 전화를 걸 수 있다.

  * Intent.ACTION_DIAL 과 tel:00000000000 데이터를 이용하여 구현하였다.








### Tab2: Gallery

> 여러 장의 이미지를 갤러리처럼 보여주는 탭으로,  이미지를 터치하면 해당 이미지만 확대하여 볼 수 있다.

![1625585385992](https://user-images.githubusercontent.com/56385667/124628029-0a6b8000-debb-11eb-8d29-4fa60e22f7cb.gif)

* 초기 화면은 1개의 GridView로 이미지를 나타냄

  * GridView는 bitmap 객체로 정의된 ImageView로 구성되어 있음

  * 이미지 파일은 res/drawable 폴더에 정의되어 있음
  
    

* 특정 이미지 터치 시 확대
  
  * ImageActivity 클래스를 정의하여 확대된 사진을 보여줌





### Tab3: Rain_notification

> 비가 오는 날 사용자가 우산을 챙겨갈 수 있도록 Notification을 보낸다.

![1625585384764](https://user-images.githubusercontent.com/56385667/124628105-1d7e5000-debb-11eb-960b-dace68193458.gif)

* 초기 화면은 TimePicker, Button, 사용자의 현재 위치정보로 이루어짐
  * 위치정보

    첫 실행 시, 사용자의 위치 권한 승인을 받아 LocationManager로 위도, 경도를 얻음

    Geocoder를 이용하여 위도, 경도 값을 주소로 변환하여 화면에 출력

  * TimePicker & Switch

    알림을 받기 원하는 시간으로 TimePicker을 설정하고 Switch를 on함

    TimePicker를 재설정할 시, Switch가 자동으로 off되고 기존에 설정되어 있던 알림은 자동으로 삭제됨

    

* Notification은 비가 오는 날, 사용자가 설정한 시간에 활성화됨.

  * 알림 조건

    기상청 날씨 API를 사용하여 현재 위치, 설정된 시간을 기반으로 강수확률을 얻음

    강수확률이 30% 이상인 날에 AlarmManager를 이용하여 사용자에게 notify함.

  * 알림 수신

    화면이 lock된 상태에서 알림 수신 시, 알림 소리와 함께 화면이 켜짐

    화면이 켜진 상태에서 알림 수신 시, 알림 소리와 함께 헤드업 알림(Heads up Notification)이 보임

  * 알림창 터치 시 앱이 실행됨

  * BroadCastReceiver를 이용하여 기기 재부팅 시에도 알림은 유지되도록 함

    

