(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TauxChangeDetailController', TauxChangeDetailController);

    TauxChangeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TauxChange', 'Devise'];

    function TauxChangeDetailController($scope, $rootScope, $stateParams, previousState, entity, TauxChange, Devise) {
        var vm = this;

        vm.tauxChange = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:tauxChangeUpdate', function(event, result) {
            vm.tauxChange = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
