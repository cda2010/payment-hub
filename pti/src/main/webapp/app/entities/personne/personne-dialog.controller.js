(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneDialogController', PersonneDialogController);

    PersonneDialogController.$inject = ['ParseLinks','$state','$uibModal','Notification','CancelAlert','Personne', 'Nationalite', 'RequestUtils', 'TypePiece', '$uibModalInstance','TableService'];

    function PersonneDialogController(ParseLinks,$state,$uibModal,Notification,CancelAlert,Personne, Nationalite, RequestUtils, TypePiece, $uibModalInstance,TableService) {
        var vm = this;

        vm.clear = clear;
        vm.typeP=$state.$current.data.typeP;
        vm.typepieces = TypePiece.query();
        vm.typePieceId = null;
        vm.referencePiece = null;
        vm.telephone = null;
        vm.item = null;
        vm.isChecked = isChecked;
        vm.toggle = toggle;
        vm.check = check;
        vm.uncheck = uncheck;
        vm.toggleAll = toggleAll;
        vm.model = null;
        vm.itemsPerPage=20;
         vm.datas =[];
        vm.nationalites = Nationalite.query();
        vm.init=function(){
            $state.reload();
        }

        function isChecked(item) {
            return vm.model === item.personneDTO.id;
        }

        function toggle(item) {
            if (vm.isChecked(item)) {
                vm.uncheck(item);
            } else {
                vm.check(item);
            }
        }

        function toggleAll() {
            if (vm.allSelected) {
                angular.forEach(vm.datas, function (val, key) {
                    vm.check(val);
                });
            } else {
                angular.forEach(vm.datas, function (val, key) {
                    vm.uncheck(val);
                });
            }
        }

        function check(item) {
            vm.model = item.personneDTO.id;
            vm.item = item;
        }

        function uncheck(item) {
            if (vm.multiple) {
                var index = vm.model.indexOf(item.id);
                vm.model.splice(index, 1);
            } else {
                vm.model = '';
            }
        }

        function clear() {
            $uibModalInstance.close(vm.item);
        }

        vm.save = function () {
            console.log('tel',vm.telephone);
            RequestUtils.FindPersonne.get({
         //       page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                "telephone": vm.telephone,
                "typeId": vm.typePieceId,
                "reference": vm.referencePiece,
                "codeTelephone": vm.codePaysTelephone
            }, function (datas,headers) {
                vm.datas = datas;
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                 vm.datas = datas;
            //    vm.page = pagingParams.page;
                console.log( vm.datas );
            }, function () {
                console.log("not found");
            });
        };
  
        //Liste des personnes disponibles 
        vm.detailsPersonne = function(datas){
            console.log('details des personnes .....');
            console.log(datas);
            $uibModal.open({
                templateUrl: 'app/entities/personne/personne-detail.html',
                controller: 'PersonneDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity : function(){
                        return datas;
                    }
                }
            }).result.then(function() {
            }, function() {
            }); 
        };




        vm.findTitulaireCompte = function () {
            console.log(vm.referenceC);
            console.log("Infos compte");
            RequestUtils.CompteTitulaire.query({
                "codeAgence": vm.codeAgence,
                "codeBanque": vm.codeBanque,
                "rib": vm.rib,
                "type": vm.type,
                "valeurComtpe": vm.referenceC
            }, function (datas) {
                vm.datas = datas;
            }, function () {
                console.log("not found");
            });
        };

        vm.setActive = function setActive(personne, isActivated) {
            console.log('traitement du  client');
            CancelAlert.confirm(function () {
                personne.activated = isActivated;
                Personne.update(personne, function () {
                    Notification.succes("Mise à jour effectuée.");
                });
            });
        }



    }
})();
