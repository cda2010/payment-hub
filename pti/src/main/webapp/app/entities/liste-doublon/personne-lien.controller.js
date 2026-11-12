(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneLienController', PersonneLienController);

    PersonneLienController.$inject = ['$state','$uibModalInstance', 'Personne','RequestUtils','Notification', 'AlertService', 'paginationConstants','entity'];

    function PersonneLienController($state,$uibModalInstance, Personne,RequestUtils,Notification, AlertService, paginationConstants,entity) {

        var vm = this;
        var j=0;
        vm.lienId=entity;
        vm.getValiderN=getValiderN;
        vm.selectEntityN=selectEntityN;
        console.log('Lien',vm.lienId);
        vm.personneDTO=vm.lienId;
        vm.flag=false;
        var personne={
            listePersonne: null,
            nomClient: null,
            telephone: null,
            listePersonneId: null
        };
        Personne.getDoublouByPieceOrHash(vm.personneDTO,function (result) {
            console.log('----',result);
            vm.datas=result;
        });

        function getValiderN() {
            console.log('-------------',vm.datas);
            vm.liste=[];
            for (var i = 0; i < vm.datas.length; i++) {
                console.log('-1.1.1');
                if (vm.datas[i].isChecked) {
                    console.log('t---',vm.datas[i]);
                    vm.liste.push(vm.datas[i].id);
                }
            }
            //personne.listePersonne=vm.liste;
            personne.listePersonneId=vm.liste;
            //personne.telephone='7895621';
            if(vm.liste.length>1){
                console.log('ddd',personne);
                RequestUtils.SaveDoublon.save(personne,function (result) {
                    console.log('liste',result);
                    Notification.success('Opération effectuée');
                    vm.clear();
                })
            } else {
                Notification.error('Veillez selectionnez au moins 2');
                vm.clear();
            }

        }
        function selectEntityN() {
            j++;
            console.log('ok',j);
            console.log('liste.isChecked');
            if(j>1){
                vm.flag=true;
            } else {
                vm.flag=false;
            }
        }

        vm.clear = function clear() {
            $uibModalInstance.close();
        }

    }
})();
