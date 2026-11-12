(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieClientDetailController', CategorieClientDetailController);

    CategorieClientDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CategorieClient'];

    function CategorieClientDetailController($scope, $rootScope, $stateParams, previousState, entity, CategorieClient) {
        var vm = this;

        vm.categorieClient = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:categorieClientUpdate', function(event, result) {
            vm.categorieClient = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
