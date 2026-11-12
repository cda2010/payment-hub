(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ApiDetailController', ApiDetailController);

    ApiDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Api', 'Methode', 'FournisseurService'];

    function ApiDetailController($scope, $rootScope, $stateParams, previousState, entity, Api, Methode, FournisseurService) {
        var vm = this;

        vm.api = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:apiUpdate', function(event, result) {
            vm.api = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
