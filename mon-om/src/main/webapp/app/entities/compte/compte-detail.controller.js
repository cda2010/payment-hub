(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('CompteDetailController', CompteDetailController);

    CompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Compte'];

    function CompteDetailController($scope, $rootScope, $stateParams, previousState, entity, Compte) {
        var vm = this;

        vm.compte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:compteUpdate', function(event, result) {
            vm.compte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
