(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$state', '$scope','$uibModal','Auth', 'Principal', 'ProfileService', 'LoginService', 'TypeService'];

    function NavbarController ($state,$scope,$uibModal, Auth, Principal, ProfileService, LoginService, TypeService) {
        var vm = this;
        vm.isNavbarCollapsed = true;
        
        Principal.identity().then(function(account) {
            vm.account = account;
            vm.nombre = 0;
            TypeService.queryTypeServiceByUserAuthorities({login:vm.account.login}, function(result) {
                vm.typeService = result;
                // for (var i=0;i<=result.length;i++){
                //     vm.nombre = vm.nombre + result[i].nombre
                // }
                console.log(vm.nombre);
            });
            
            if(account==null){
                vm.isAuthenticated = Principal.isAuthenticated;
            }else{
                if(account.resetKey==null){
                    vm.isAuthenticated = Principal.isAuthenticated;
                }else{
                    vm.isAuthenticated = null;
                }
            }
        });


        ProfileService.getProfileInfo().then(function(response) {
            vm.inProduction = response.inProduction;
            vm.swaggerEnabled = response.swaggerEnabled;
        });

        
        vm.login = login;
        vm.logout = logout;
        vm.toggleNavbar = toggleNavbar;
        vm.collapseNavbar = collapseNavbar;
        vm.qRcode=qRcode;
        vm.$state = $state;

        
        function qRcode() {
            $uibModal.open({
                templateUrl: 'app/components/login/qrCode.html',
                controller: 'QrcodeModalController',
                controllerAs: vm,
                backdrop: 'static',
                scope: $scope,
                size: 'md',
                resolve: {
                    entity: function () {
                        return 'vm.cptUO'
                    }
                }

            }).result.then(function () {
            }, function () {

            });
        }
        function login() {
            collapseNavbar();
            LoginService.open();
        }

        function logout() {
            //collapseNavbar();
            Auth.logout();
        }


        function toggleNavbar() {
            vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
        }

        function collapseNavbar() {
            vm.isNavbarCollapsed = true;
        }
        init();
        function init(){
            
            $("#sidebar-collapse").on('click', function () {
                if (!$('#sidebar').is(':visible'))
                    $("#sidebar").toggleClass("hide");
                $("#sidebar").toggleClass("menu-compact");
                $(".page-c").toggleClass("menu-compact");
                $(".sidebar-collapse").toggleClass("active");
              var b = $("#sidebar").hasClass("menu-compact");

                if ($(".sidebar-menu").closest("div").hasClass("slimScrollDiv")) {
                    $(".sidebar-menu").slimScroll({ destroy: true });
                    $(".sidebar-menu").attr('style', '');
                }
                if (b) {
                    $(".open > .submenu")
                        .removeClass("open");
                } else {
                    if ($('.page-sidebar').hasClass('sidebar-fixed')) {
                        var position = (readCookie("rtl-support") || location.pathname == "/index-rtl-fa.html" || location.pathname == "/index-rtl-ar.html") ? 'right' : 'left';
                        $('.sidebar-menu').slimscroll({
                            height: 'auto',
                            position: position,
                            size: '3px',
                            color: themeprimary
                        });
                    }
                }
                //Slim Scroll Handle
            });
        }
    }
})();
