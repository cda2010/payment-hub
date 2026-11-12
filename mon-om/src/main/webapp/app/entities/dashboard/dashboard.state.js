// Copie du contenu de home.state.js pour dashboard.state.js
// ... existing code ... 

(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('dashboard', {
            parent: 'dashboard',
            url: '/dashboard',
            data: {
                authorities: ['ROLE_CONSULT_TDB']
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/dashboard/dashboard.html',
                    controller: 'DashboardController',
                    controllerAs: 'vm'
                }
            }
        });
    }
})();