(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BeneficiaireDetailController', BeneficiaireDetailController);

    BeneficiaireDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Beneficiaire', 'Personne', 'Operation'];

    function BeneficiaireDetailController($scope, $rootScope, $stateParams, previousState, entity, Beneficiaire, Personne, Operation) {
        var vm = this;

        vm.beneficiaire = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:beneficiaireUpdate', function(event, result) {
            vm.beneficiaire = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
