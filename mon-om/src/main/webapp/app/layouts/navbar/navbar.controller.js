
(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$state', 'Auth', 'Principal', 'ProfileService', 'LoginService', '$rootScope'];

    function NavbarController($state, Auth, Principal, ProfileService, LoginService, $rootScope) {
        var vm = this;

        vm.isNavbarCollapsed = true;
        vm.isAuthenticated = Principal.isAuthenticated;
        vm.ouv = ouv;
        ProfileService.getProfileInfo().then(function (response) {
            vm.inProduction = response.inProduction;
            vm.swaggerEnabled = response.swaggerEnabled;
        });

        vm.login = login;
        vm.logout = logout;
        vm.toggleNavbar = toggleNavbar;
        vm.collapseNavbar = collapseNavbar;
        vm.$state = $state;

        function login() {
            collapseNavbar();
            LoginService.open();
        }

        function logout() {
            collapseNavbar();
            Auth.logout();
            $state.go('connexion');
        }

        function toggleNavbar() {
            vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
        }

        function ouv() {
            $rootScope.custom = $rootScope.custom === false ? true : false;
        }


        function collapseNavbar() {
         
            vm.isNavbarCollapsed = true;
        }
            getAccount();
        function getAccount() {
            Principal.identity().then(function (account) {
                vm.account = account;

            });
        }

    }
})();
