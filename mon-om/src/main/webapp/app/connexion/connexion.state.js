(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('connexion', {
            parent:'app',
            url: '/connexion',
            data: {
                authorities: [],
                pageTitle: 'Registration'
            },
            views: {
                'root@': {
                    templateUrl: 'app/connexion/connexion.html',
                    controller: 'ConnexionController',
                    controllerAs: 'vm'
                }
            }
        });
    }
})();
