/**
 * 倚窗听雨（秦维亮 ）制作
 * 组件封装
 */
//京东页面尾部
Vue.component('jd-footer', {
	template: '<div class="container text-center footer">						<ul class="row nav nav-pills ">				<li>					<a href="#"> 关于我们 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 人才招聘 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 商家入驻 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 广告服务 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 手机京东 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 友情链接 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 销售联盟 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 京东社区 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 京东公益 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> 京东公益 &nbsp; &nbsp; |</a>				</li>				<li>					<a href="#"> English Site &nbsp; &nbsp;</a>				</li>			</ul>			<div class="row foot_but "> Copyright &nbsp;© &nbsp;2004 - 2018 &nbsp; &nbsp;京东JD.com &nbsp;版权所有</div>		</div>'
})

//顶部搜索
Vue.component('jd-search', {
	data: function() {
		return {
			query: '曲面屏'
		}
	},
	template: '<div class="container head_search">			<div class="row">				<div class="col-md-4 text-center"><img src="img/jdlogo2.png" /></div>				<div class="col-md-8">					<div class="col-md-6"><input type="search" class="form-control" v-model="query" /></div>					<div class="col-md-6 soubutton">						<a href="#" class="btn btn-info btn-lg"  @click="fun"><span class="glyphicon glyphicon-search">搜索</span></a>					</div>				</div>			</div>		</div>',
	methods: {
		fun() {
			sessionStorage.clear();
			sessionStorage.setItem('query', this.query);
			window.location.href = "products.html";
		}
	}
})