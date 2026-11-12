(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('partial', {
            abstract: true,
            parent :'app',
            views: {
                'navbar': {
                    templateUrl: 'app/layouts/navbar/navbar.html',
                    controller: 'NavbarController',
                    controllerAs: 'vm'
                },
                'sidebar': {
                    templateUrl: 'app/layouts/navbar/sidebar.html',
                    controller: 'SidebarController',
                    controllerAs: 'vm'
                }
            }

        });
    }
})();

