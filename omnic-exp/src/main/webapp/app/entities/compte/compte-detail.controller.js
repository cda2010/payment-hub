(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('CompteDetailController', CompteDetailController);

    CompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Compte'];

    function CompteDetailController($scope, $rootScope, $stateParams, previousState, entity, Compte) {
        var vm = this;

        vm.compte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:compteUpdate', function(event, result) {
            vm.compte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
